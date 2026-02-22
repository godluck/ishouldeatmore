package com.gdlk.ishouldeatmore;

import com.gdlk.ishouldeatmore.item.*;
import com.gdlk.ishouldeatmore.mixin.FoodDataMixin;
import com.gdlk.ishouldeatmore.network.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.client.event.InputEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Ishouldeatmore.MODID)
public class Ishouldeatmore {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ishouldeatmore";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Delay in server ticks before applying lightning damage (20 ≈ 1 second). */
    private static final int LIGHTNING_DAMAGE_DELAY_TICKS = 20;
    private static final List<PendingLightningDamage> pendingLightningDamage = new ArrayList<>();

    private static final class PendingLightningDamage {
        final UUID playerId;
        final float damagePerHit;
        int numHits;
        final int newStage;
        int ticksRemaining;
        final int delayPerHit;

        PendingLightningDamage(UUID playerId, float damagePerHit, int numHits, int newStage, int delayPerHit) {
            this.playerId = playerId;
            this.damagePerHit = damagePerHit;
            this.numHits = numHits;
            this.newStage = newStage;
            this.ticksRemaining = delayPerHit;
            this.delayPerHit = delayPerHit;
        }
    }

    // Create a Deferred Register to hold Blocks which will all be registered under
    // the "ishouldeatmore" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under
    // the "ishouldeatmore" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be
    // registered under the "ishouldeatmore" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> FAT_SWORD = ITEMS.registerItem("fat_sword", FatSword::new,
            new Item.Properties().durability(100));
    public static final DeferredItem<Item> MUSCLE_SWORD = ITEMS.registerItem("muscle_sword", MuscleSword::new,
            new Item.Properties().durability(200));
    public static final DeferredItem<Item> BONE_SWORD = ITEMS.registerItem("bone_sword", BoneSword::new,
            new Item.Properties().durability(300));
    public static final DeferredItem<Item> ARM_SWORD = ITEMS.registerItem("arm_sword", ArmSword::new,
            new Item.Properties().durability(600));
    public static final DeferredItem<Item> GOLDEN_ARM_SWORD = ITEMS.registerItem("golden_arm_sword",
            GoldenArmSword::new, new Item.Properties().durability(2000));

    // Armor sets by tier (Fat=Leather, Muscle=Chain, Bone=Iron, Arm=Diamond, Golden
    // Arm=Netherite)
    // All are FoodArmor: damage is reduced by consuming food level and saturation
    // when wearing any piece
    public static final DeferredItem<Item> FAT_HELMET = ITEMS.register("fat_helmet",
            () -> new FoodArmor(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties().durability(100)));
    public static final DeferredItem<Item> FAT_CHESTPLATE = ITEMS.register("fat_chestplate",
            () -> new FoodArmor(ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(100)));
    public static final DeferredItem<Item> FAT_LEGGINGS = ITEMS.register("fat_leggings",
            () -> new FoodArmor(ArmorMaterials.LEATHER, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(100)));
    public static final DeferredItem<Item> FAT_BOOTS = ITEMS.register("fat_boots",
            () -> new FoodArmor(ArmorMaterials.LEATHER, ArmorItem.Type.BOOTS, new Item.Properties().durability(100)));
    public static final DeferredItem<Item> MUSCLE_HELMET = ITEMS.register("muscle_helmet",
            () -> new FoodArmor(ArmorMaterials.CHAIN, ArmorItem.Type.HELMET, new Item.Properties().durability(200)));
    public static final DeferredItem<Item> MUSCLE_CHESTPLATE = ITEMS.register("muscle_chestplate",
            () -> new FoodArmor(ArmorMaterials.CHAIN, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(200)));
    public static final DeferredItem<Item> MUSCLE_LEGGINGS = ITEMS.register("muscle_leggings",
            () -> new FoodArmor(ArmorMaterials.CHAIN, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(200)));
    public static final DeferredItem<Item> MUSCLE_BOOTS = ITEMS.register("muscle_boots",
            () -> new FoodArmor(ArmorMaterials.CHAIN, ArmorItem.Type.BOOTS, new Item.Properties().durability(200)));
    public static final DeferredItem<Item> BONE_HELMET = ITEMS.register("bone_helmet",
            () -> new FoodArmor(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties().durability(300)));
    public static final DeferredItem<Item> BONE_CHESTPLATE = ITEMS.register("bone_chestplate",
            () -> new FoodArmor(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(300)));
    public static final DeferredItem<Item> BONE_LEGGINGS = ITEMS.register("bone_leggings",
            () -> new FoodArmor(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(300)));
    public static final DeferredItem<Item> BONE_BOOTS = ITEMS.register("bone_boots",
            () -> new FoodArmor(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, new Item.Properties().durability(300)));
    public static final DeferredItem<Item> ARM_HELMET = ITEMS.register("arm_helmet",
            () -> new FoodArmor(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties().durability(600)));
    public static final DeferredItem<Item> ARM_CHESTPLATE = ITEMS.register("arm_chestplate",
            () -> new FoodArmor(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(600)));
    public static final DeferredItem<Item> ARM_LEGGINGS = ITEMS.register("arm_leggings",
            () -> new FoodArmor(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(600)));
    public static final DeferredItem<Item> ARM_BOOTS = ITEMS.register("arm_boots",
            () -> new FoodArmor(ArmorMaterials.DIAMOND, ArmorItem.Type.BOOTS, new Item.Properties().durability(600)));
    public static final DeferredItem<Item> GOLDEN_ARM_HELMET = ITEMS.register("golden_arm_helmet",
            () -> new FoodArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(2000)));
    public static final DeferredItem<Item> GOLDEN_ARM_CHESTPLATE = ITEMS.register("golden_arm_chestplate",
            () -> new FoodArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(2000)));
    public static final DeferredItem<Item> GOLDEN_ARM_LEGGINGS = ITEMS.register("golden_arm_leggings",
            () -> new FoodArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(2000)));
    public static final DeferredItem<Item> GOLDEN_ARM_BOOTS = ITEMS.register("golden_arm_boots",
            (props) -> new FoodArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(2000)));

    // Creates a creative tab with the id "ishouldeatmore:food_tool_tab" for the
    // example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS
            .register("food_tool_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ishouldeatmore")) // The language key for the title of your
                                                                               // CreativeModeTab
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> FAT_SWORD.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(FAT_SWORD.get());
                        output.accept(MUSCLE_SWORD.get());
                        output.accept(BONE_SWORD.get());
                        output.accept(ARM_SWORD.get());
                        output.accept(GOLDEN_ARM_SWORD.get());
                        output.accept(FAT_HELMET.get());
                        output.accept(FAT_CHESTPLATE.get());
                        output.accept(FAT_LEGGINGS.get());
                        output.accept(FAT_BOOTS.get());
                        output.accept(MUSCLE_HELMET.get());
                        output.accept(MUSCLE_CHESTPLATE.get());
                        output.accept(MUSCLE_LEGGINGS.get());
                        output.accept(MUSCLE_BOOTS.get());
                        output.accept(BONE_HELMET.get());
                        output.accept(BONE_CHESTPLATE.get());
                        output.accept(BONE_LEGGINGS.get());
                        output.accept(BONE_BOOTS.get());
                        output.accept(ARM_HELMET.get());
                        output.accept(ARM_CHESTPLATE.get());
                        output.accept(ARM_LEGGINGS.get());
                        output.accept(ARM_BOOTS.get());
                        output.accept(GOLDEN_ARM_HELMET.get());
                        output.accept(GOLDEN_ARM_CHESTPLATE.get());
                        output.accept(GOLDEN_ARM_LEGGINGS.get());
                        output.accept(GOLDEN_ARM_BOOTS.get());
                    }).build());

    private static final ResourceLocation FOOD_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation FOOD_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_half");

    // The constructor for the mod class is the first code that is run when your mod
    // is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and
    // pass them in automatically.
    public Ishouldeatmore(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class
        // (ishouldeatmore) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in
        // this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, this::registerPayloads);

        // Register our mod's ModConfigSpec so that FML can create and load the config
        // file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncFoodEatenPayload.TYPE, SyncFoodEatenPayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                var player = Minecraft.getInstance().player;
                if (player != null && player.getFoodData() instanceof FoodDataSync sync) {
                    sync.ishouldeatmore$setFoodEaten(payload.foodEaten());
                }
            });
        });
        registrar.playToClient(FoodLevelStagePayload.TYPE, FoodLevelStagePayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                var player = Minecraft.getInstance().player;
                if (player != null && player.getFoodData() instanceof FoodDataSync sync) {
                    sync.ishouldeatmore$setFoodLevelStage(payload.stage());
                }
            });
        });
        registrar.playToServer(AirJumpPayload.TYPE, AirJumpPayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (FoodArmor.isWearingFoodLeggings(player)) {
                        Vec3 motion = player.getDeltaMovement();
                        float consumed = 0.01f;
                        if (motion.y < 0.05 && payload.delta() == 0) {
                            // hover when delta is 0
                            player.setDeltaMovement(motion.x, 0, motion.z);
                        }
                        if (payload.delta() > 0) {
                            // jump if delta > 0
                            player.setDeltaMovement(new Vec3(motion.x, payload.delta(), motion.z));
                            consumed = 1f;
                        }
                        float saturationLevel = player.getFoodData().getSaturationLevel();
                        if (saturationLevel >= consumed) {
                            player.getFoodData().setSaturation(saturationLevel - consumed);
                        } else {
                            player.getFoodData().setSaturation(0);
                        }
                    }
                }
            });
        });
        registrar.playToServer(DashPayload.TYPE, DashPayload.STREAM_CODEC, ((payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (FoodArmor.isWearingFoodLeggings(player)) {
                        float consumed = 1f;
                        player.addDeltaMovement(player.getLookAngle().normalize().scale(0.7));
                        float saturationLevel = player.getFoodData().getSaturationLevel();
                        if (saturationLevel >= consumed) {
                            player.getFoodData().setSaturation(saturationLevel - consumed);
                        } else {
                            player.getFoodData().setSaturation(0);
                        }
                    }
                }
            });
        }));
    }

    @SubscribeEvent
    public void onFoodEat(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack food = event.getItem();
            FoodProperties foodProperties = food.getFoodProperties(null);
            boolean shouldApply = foodProperties != null && foodProperties.nutrition() > 0;
            if (shouldApply) {
                event.setDuration(0);
            }
        }
    }

    public static boolean singleLighting(ServerPlayer player, float damage){
        var damageSource = player.level().damageSources().source(DamageTypes.LIGHTNING_BOLT);
        if (player.hurt(damageSource, damage)){
            // Spawn visual-only lightning bolt
            LightningBolt bolt = new LightningBolt(net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, player.level());
            bolt.setPos(player.getX(), player.getY(), player.getZ());
            bolt.setVisualOnly(true);
            player.level().addFreshEntity(bolt);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack food = event.getItem();
            FoodProperties foodProperties = food.getFoodProperties(null);
            boolean shouldApply = foodProperties != null && foodProperties.nutrition() > 0;
            if (shouldApply) {
                FoodData foodData = player.getFoodData();
                if (foodData instanceof FoodDataSync foodDataSync
                        && Math.log10(foodData.getFoodLevel()) == Math.max(2,
                        foodDataSync.ishouldeatmore$getFoodLevelStage() + 1)) {
                    if (player instanceof ServerPlayer serverPlayer){
                        PacketDistributor.sendToPlayer(serverPlayer,
                                new SyncFoodEatenPayload(foodDataSync.ishouldeatmore$getFoodEaten()));
                        // Schedule damage and stage update after delay so damage is separated
                        int foodLevelStage = foodDataSync.ishouldeatmore$getFoodLevelStage();
                        float hurtDamage = (float) Math.pow(10, foodLevelStage) * 2;
                        synchronized (pendingLightningDamage) {
                            if (pendingLightningDamage.stream().noneMatch(i -> i.playerId == serverPlayer.getUUID())){
                                pendingLightningDamage.add(new PendingLightningDamage(
                                        serverPlayer.getUUID(), hurtDamage, foodLevelStage, foodLevelStage + 1, LIGHTNING_DAMAGE_DELAY_TICKS));
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        synchronized (pendingLightningDamage) {
            Iterator<PendingLightningDamage> it = pendingLightningDamage.iterator();
            while (it.hasNext()) {
                PendingLightningDamage pending = it.next();
                if (pending.ticksRemaining > 0){
                    pending.ticksRemaining--;
                }
                if (pending.ticksRemaining > 0) continue;
                ServerPlayer serverPlayer = event.getServer().getPlayerList().getPlayer(pending.playerId);
                if (serverPlayer == null || !serverPlayer.isAlive()) {
                    it.remove();
                    continue;
                }
                if (singleLighting(serverPlayer, pending.damagePerHit)){
                    pending.numHits--;
                    pending.ticksRemaining = pending.delayPerHit;
                    if (serverPlayer.getFoodData() instanceof FoodDataSync foodDataSync && pending.numHits == 0) {
                        it.remove();
                        foodDataSync.ishouldeatmore$setFoodLevelStage(pending.newStage);
                        PacketDistributor.sendToPlayer(serverPlayer,
                                new FoodLevelStagePayload(foodDataSync.ishouldeatmore$getFoodLevelStage()));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer && player.getFoodData() instanceof FoodDataSync sync) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncFoodEatenPayload(sync.ishouldeatmore$getFoodEaten()));
            PacketDistributor.sendToPlayer(serverPlayer,
                    new FoodLevelStagePayload(sync.ishouldeatmore$getFoodLevelStage()));
        }
    }

    @SubscribeEvent
    public void onRenderFood(RenderGuiLayerEvent.Pre event) {
        if (event.getName() != VanillaGuiLayers.FOOD_LEVEL) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        assert player != null;
        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        int saturationLevel = (int) foodData.getSaturationLevel();
        int foodLevelStage = -1;
        if (foodData instanceof FoodDataSync) {
            foodLevelStage = ((FoodDataSync) foodData).ishouldeatmore$getFoodLevelStage();
        }

        int y = mc.getWindow().getGuiScaledHeight() - mc.gui.rightHeight - 10;
        int x = mc.getWindow().getGuiScaledWidth() / 2 + 10;

        RenderSystem.enableBlend();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int iconCount = Math.max(1, foodLevelStage);
        for (int i = 0; i < iconCount; i++) {
            guiGraphics.blitSprite(FOOD_EMPTY_SPRITE, x + 8 * i, y, 9, 9);
            guiGraphics.blitSprite(FOOD_HALF_SPRITE, x + 8 * i, y, 9, 9);
        }
        String countString = """
                *%d (+%d)""".formatted(foodLevel, saturationLevel);
        guiGraphics.drawString(mc.gui.getFont(), Component.literal(countString), x + 8 * iconCount, y, 0xffffff);
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public void onPlayerHitted(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (FoodArmor.countFoodArmorPieces(player) == 0) {
                return;
            }
            int foodLevel = player.getFoodData().getFoodLevel();
            float saturationLevel = player.getFoodData().getSaturationLevel();
            if (getFoodLevelStage() < 2) {
                return;
            }
            float originalDamage = event.getAmount();
            // Tier-based cap: full set fat=30%, full set golden_arm=100%; mixed sets use
            // average tier
            float effectiveMaxReduction = FoodArmor.getEffectiveMaxReduction(player);
            float damageToAbsorb = originalDamage * effectiveMaxReduction;
            float pool = saturationLevel + foodLevel;
            float actuallyAbsorbed = Math.min(damageToAbsorb, pool);
            if (actuallyAbsorbed <= 0) {
                return;
            }
            // Consume saturation first, then food
            if (saturationLevel >= actuallyAbsorbed) {
                player.getFoodData().setSaturation(saturationLevel - actuallyAbsorbed);
            } else if (saturationLevel + foodLevel <= actuallyAbsorbed) {
                player.getFoodData().setFoodLevel(0);
                player.getFoodData().setSaturation(0);
            } else {
                player.getFoodData().setSaturation(0);
                player.getFoodData()
                        .setFoodLevel(Math.max(0, (int) Math.floor(foodLevel + saturationLevel - actuallyAbsorbed)));
            }
            event.setAmount(originalDamage - actuallyAbsorbed);
        }
    }

    @SubscribeEvent
    public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        ItemStack original = event.getOriginal();
        Player player = event.getEntity();
        if (original.getItem() instanceof FatSword) {
            ItemStack muscle = new ItemStack(MUSCLE_SWORD.asItem(), 1);
            if (event.getHand() != null) {
                player.setItemInHand(event.getHand(), muscle);
            }
        }
    }

    @SubscribeEvent
    public void onArmorHurt(ArmorHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Damage equipped food armor when hit (server-only so stacks sync); split among
            // worn pieces only
            if (!player.level().isClientSide()) {
                EquipmentSlot[] armorSlots = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                        EquipmentSlot.FEET };
                for (EquipmentSlot slot : armorSlots) {
                    ItemStack armorStack = player.getItemBySlot(slot);
                    if (!armorStack.isEmpty() && armorStack.getItem() instanceof FoodArmor) {
                        if (armorStack.getMaxDamage() - armorStack.getDamageValue() <= 1) {
                            Item replacement = null;
                            if (armorStack.is(FAT_HELMET.get())) {
                                replacement = MUSCLE_HELMET.get();
                            } else if (armorStack.is(FAT_CHESTPLATE.get())) {
                                replacement = MUSCLE_CHESTPLATE.get();
                            } else if (armorStack.is(FAT_LEGGINGS.get())) {
                                replacement = MUSCLE_LEGGINGS.get();
                            } else if (armorStack.is(FAT_BOOTS.get())) {
                                replacement = MUSCLE_BOOTS.get();
                            }
                            if (replacement != null) {
                                player.setItemSlot(slot, new ItemStack(replacement, 1));
                            }
                        }
                    }
                }
            }
        }

    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty())
            return;
        String id = stack.getDescriptionId();
        if (id.startsWith("item." + MODID + ".")) {
            event.getToolTip().add(Component.translatable(id + ".tooltip").withStyle(ChatFormatting.GRAY));
            if (stack.is(FAT_SWORD.get()) || stack.is(FAT_HELMET.get()) || stack.is(FAT_CHESTPLATE.get())
                    || stack.is(FAT_LEGGINGS.get()) || stack.is(FAT_BOOTS.get())) {
                event.getToolTip().add(Component.translatable("item.ishouldeatmore.craft_tip.fat_to_muscle")
                        .withStyle(ChatFormatting.DARK_GREEN));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Minecraft mc = Minecraft.getInstance();
        boolean jumpPressed = mc.options.keyJump.isDown();
        if (jumpPressed && FoodArmor.isWearingFoodLeggings(player) && player.level().isClientSide) {
            if (getFoodLevelStage() >= 3) {
                Vec3 motion = player.getDeltaMovement();
                if (motion.y < 0.1) {
                    player.setDeltaMovement(motion.x, 0, motion.z);
                }
                PacketDistributor.sendToServer(new AirJumpPayload(0));
            }
        }
    }

    @SubscribeEvent
    public void onAirJump(InputEvent.Key event) {
        KeyMapping keyJump = Minecraft.getInstance().options.keyJump;
        if (isKeyPressed(event, keyJump, 4)) {
            keyJump.consumeClick();
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            Vec3 currentDelta = player.getDeltaMovement();
            player.setDeltaMovement(new Vec3(currentDelta.x, 0.7, currentDelta.z));
            PacketDistributor.sendToServer(new AirJumpPayload(0.7));
        }

        KeyMapping keySprint = Minecraft.getInstance().options.keySprint;
        if (isKeyPressed(event, keySprint, 5)) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || player.onGround()) {
                return;
            }
            keySprint.consumeClick();
            player.addDeltaMovement(player.getLookAngle().normalize().scale(0.7));
            PacketDistributor.sendToServer(new DashPayload(0.7));
        }
    }

    private static boolean isKeyPressed(InputEvent.Key event, KeyMapping key, int foodLevelStageLimit) {
        if (!key.matches(event.getKey(), event.getScanCode())) {
            return false;
        }
        if (!isInGame()) {
            return false;
        }
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return false;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isShiftKeyDown()) {
            return false;
        }
        if (getFoodLevelStage() < foodLevelStageLimit) {
            return false;
        }
        return true;
    }

    private static boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (mc.getOverlay() != null) {
            return false;
        }
        // 不能打开任何 GUI
        if (mc.screen != null) {
            return false;
        }
        // 当前窗口捕获鼠标操作
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        // 选择了当前窗口
        return mc.isWindowActive();
    }

    private static int getFoodLevelStage() {
        if (Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.getFoodData() instanceof FoodDataSync foodData) {
            return foodData.ishouldeatmore$getFoodLevelStage();
        }
        return 0;
    }
}
