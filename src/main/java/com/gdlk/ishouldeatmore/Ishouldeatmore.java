package com.gdlk.ishouldeatmore;

import com.gdlk.ishouldeatmore.item.FoodSword;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

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


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Ishouldeatmore.MODID)
public class Ishouldeatmore {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ishouldeatmore";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "ishouldeatmore" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "ishouldeatmore" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "ishouldeatmore" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> FOOD_SWORD = ITEMS.registerItem("food_sword", FoodSword::new);
    // Creates a creative tab with the id "ishouldeatmore:food_tool_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("food_tool_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ishouldeatmore")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> FOOD_SWORD.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FOOD_SWORD.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    private static final ResourceLocation FOOD_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation FOOD_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_half");

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Ishouldeatmore(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ishouldeatmore) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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

    @SubscribeEvent
    public void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack food = event.getItem();
            FoodProperties foodProperties = food.getFoodProperties(null);
            boolean shouldApply = foodProperties != null && foodProperties.nutrition() > 0;
            if (shouldApply) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        120000,
                        5
                ));
                player.addEffect(new MobEffectInstance(
                        MobEffects.HUNGER,
                        1200,
                        20
                ));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()){
            return;
        }
        Player originPlayer = event.getOriginal();
        Player player = event.getEntity();
        player.getFoodData().setFoodLevel(originPlayer.getFoodData().getFoodLevel());
        player.getFoodData().setSaturation(originPlayer.getFoodData().getSaturationLevel());
        player.getFoodData().setExhaustion(originPlayer.getFoodData().getExhaustionLevel());
    }

    @SubscribeEvent
    public void onRenderFood(RenderGuiLayerEvent.Pre event) {
        if (event.getName() != VanillaGuiLayers.FOOD_LEVEL){
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        assert player != null;
        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        int saturationLevel = (int)foodData.getSaturationLevel();

        int y = mc.getWindow().getGuiScaledHeight() - mc.gui.rightHeight - 10;
        int x = mc.getWindow().getGuiScaledWidth() / 2 + 10;

        RenderSystem.enableBlend();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        guiGraphics.blitSprite(FOOD_EMPTY_SPRITE, x, y, 9, 9);
        guiGraphics.blitSprite(FOOD_HALF_SPRITE, x, y, 9, 9);
        String countString = """
                *%d (+%d)""".formatted(foodLevel, saturationLevel);
        guiGraphics.drawString(mc.gui.getFont(), Component.literal(countString),x + 9, y, 0xffffff);
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public void onPlayerHitted(LivingIncomingDamageEvent event){
        if (event.getEntity() instanceof Player player){
            int foodLevel = player.getFoodData().getFoodLevel();
            int saturationLevel = (int)player.getFoodData().getSaturationLevel();
            if (Math.log10(foodLevel)<2){
                return;
            }
            float originalDamage = event.getOriginalAmount();
            if (originalDamage < saturationLevel){
                player.getFoodData().setSaturation(saturationLevel - originalDamage);
                event.setAmount(0);
            } else if (originalDamage < foodLevel + saturationLevel){
                player.getFoodData().setFoodLevel((int) Math.floor(foodLevel + saturationLevel - originalDamage));
                player.getFoodData().setSaturation(0);
                event.setAmount(0);
            } else {
                player.getFoodData().setFoodLevel(0);
                player.getFoodData().setSaturation(0);
                event.setAmount(originalDamage - saturationLevel - foodLevel);
            }
        }
    }

//    @SubscribeEvent void onPlayerAttack(LivingDamageEvent.Pre event){
//        if (event.getSource().getEntity() instanceof Player player){
//            int foodLevel = player.getFoodData().getFoodLevel();
//            int saturationLevel = (int)player.getFoodData().getSaturationLevel();
//            float attackModifier = (float) Math.log10(foodLevel);
//            if (attackModifier < 1){
//                return;
//            }
//            if (attackModifier < saturationLevel){
//                player.getFoodData().setSaturation(saturationLevel - attackModifier);
//            } else if (attackModifier < foodLevel + saturationLevel){
//                player.getFoodData().setFoodLevel((int) Math.floor(foodLevel + saturationLevel - attackModifier));
//                player.getFoodData().setSaturation(0);
//            } else {
//                return;
//            }
//            float originalDamage = event.getOriginalDamage();
//            event.setNewDamage(originalDamage + attackModifier*attackModifier);
//        }
//    }
}
