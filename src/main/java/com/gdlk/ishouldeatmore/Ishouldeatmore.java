package com.gdlk.ishouldeatmore;

import com.gdlk.ishouldeatmore.data.LightningDamageScheduler;
import com.gdlk.ishouldeatmore.event.FoodEvents;
import com.gdlk.ishouldeatmore.event.ItemTooltipHandler;
import com.gdlk.ishouldeatmore.event.PlayerEvents;
import com.gdlk.ishouldeatmore.event.ServerTickHandler;
import com.gdlk.ishouldeatmore.item.*;
import com.gdlk.ishouldeatmore.network.PayloadHandlers;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Ishouldeatmore.MODID)
public class Ishouldeatmore {

    public static final String MODID = "ishouldeatmore";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Registries
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Swords (Food Sword tier: Fat → Muscle → Bone → Arm → Golden Arm)
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

    // Armor (Fat=Leather, Muscle=Chain, Bone=Iron, Arm=Diamond, Golden Arm=Netherite)
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
            () -> new FoodArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(2000)));

    // Creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS
            .register("food_tool_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ishouldeatmore"))
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

    public Ishouldeatmore(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        LightningDamageScheduler lightningScheduler = new LightningDamageScheduler();
        NeoForge.EVENT_BUS.register(new FoodEvents(lightningScheduler));
        NeoForge.EVENT_BUS.register(new PlayerEvents());
        NeoForge.EVENT_BUS.register(new ItemTooltipHandler());
        NeoForge.EVENT_BUS.register(new ServerTickHandler(lightningScheduler));

        modEventBus.addListener(RegisterPayloadHandlersEvent.class, PayloadHandlers::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
