package com.gdlk.ishouldeatmore.datagen;

import com.gdlk.ishouldeatmore.Ishouldeatmore;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Generates item tags for enchantable categories (vanilla minecraft:enchantable/*),
 * adding this mod's swords and armor so they can receive the appropriate enchantments.
 * Outputs to the minecraft namespace so tags merge additively with vanilla.
 *
 * @see <a href="https://docs.neoforged.net/docs/1.21.1/resources/server/tags/#datagen">NeoForge Tags Datagen</a>
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    private static final TagKey<Item> ENCHANTABLE_DURABILITY = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/durability"));
    private static final TagKey<Item> ENCHANTABLE_ARMOR = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/armor"));
    private static final TagKey<Item> ENCHANTABLE_SWORD = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/sword"));
    private static final TagKey<Item> ENCHANTABLE_HEAD_ARMOR = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/head_armor"));
    private static final TagKey<Item> ENCHANTABLE_CHEST_ARMOR = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/chest_armor"));
    private static final TagKey<Item> ENCHANTABLE_LEG_ARMOR = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/leg_armor"));
    private static final TagKey<Item> ENCHANTABLE_FOOT_ARMOR = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/foot_armor"));
    private static final TagKey<Item> ENCHANTABLE_FIRE_ASPECT = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/fire_aspect"));
    private static final TagKey<Item> ENCHANTABLE_SHARP_WEAPON = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("minecraft", "enchantable/sharp_weapon"));

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagsProvider.TagLookup<Block>> blockTagLookup,
                              ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagLookup, "minecraft", existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        // Durability (Unbreaking, Mending): all swords and armor
        tag(ENCHANTABLE_DURABILITY)
                .add(Ishouldeatmore.FAT_SWORD.get(), Ishouldeatmore.MUSCLE_SWORD.get(), Ishouldeatmore.BONE_SWORD.get(),
                        Ishouldeatmore.ARM_SWORD.get(), Ishouldeatmore.GOLDEN_ARM_SWORD.get())
                .add(Ishouldeatmore.FAT_HELMET.get(), Ishouldeatmore.FAT_CHESTPLATE.get(), Ishouldeatmore.FAT_LEGGINGS.get(), Ishouldeatmore.FAT_BOOTS.get())
                .add(Ishouldeatmore.MUSCLE_HELMET.get(), Ishouldeatmore.MUSCLE_CHESTPLATE.get(), Ishouldeatmore.MUSCLE_LEGGINGS.get(), Ishouldeatmore.MUSCLE_BOOTS.get())
                .add(Ishouldeatmore.BONE_HELMET.get(), Ishouldeatmore.BONE_CHESTPLATE.get(), Ishouldeatmore.BONE_LEGGINGS.get(), Ishouldeatmore.BONE_BOOTS.get())
                .add(Ishouldeatmore.ARM_HELMET.get(), Ishouldeatmore.ARM_CHESTPLATE.get(), Ishouldeatmore.ARM_LEGGINGS.get(), Ishouldeatmore.ARM_BOOTS.get())
                .add(Ishouldeatmore.GOLDEN_ARM_HELMET.get(), Ishouldeatmore.GOLDEN_ARM_CHESTPLATE.get(), Ishouldeatmore.GOLDEN_ARM_LEGGINGS.get(), Ishouldeatmore.GOLDEN_ARM_BOOTS.get());

        // General armor enchantments
        tag(ENCHANTABLE_ARMOR)
                .add(Ishouldeatmore.FAT_HELMET.get(), Ishouldeatmore.FAT_CHESTPLATE.get(), Ishouldeatmore.FAT_LEGGINGS.get(), Ishouldeatmore.FAT_BOOTS.get())
                .add(Ishouldeatmore.MUSCLE_HELMET.get(), Ishouldeatmore.MUSCLE_CHESTPLATE.get(), Ishouldeatmore.MUSCLE_LEGGINGS.get(), Ishouldeatmore.MUSCLE_BOOTS.get())
                .add(Ishouldeatmore.BONE_HELMET.get(), Ishouldeatmore.BONE_CHESTPLATE.get(), Ishouldeatmore.BONE_LEGGINGS.get(), Ishouldeatmore.BONE_BOOTS.get())
                .add(Ishouldeatmore.ARM_HELMET.get(), Ishouldeatmore.ARM_CHESTPLATE.get(), Ishouldeatmore.ARM_LEGGINGS.get(), Ishouldeatmore.ARM_BOOTS.get())
                .add(Ishouldeatmore.GOLDEN_ARM_HELMET.get(), Ishouldeatmore.GOLDEN_ARM_CHESTPLATE.get(), Ishouldeatmore.GOLDEN_ARM_LEGGINGS.get(), Ishouldeatmore.GOLDEN_ARM_BOOTS.get());

        // Sword enchantments
        tag(ENCHANTABLE_SWORD)
                .add(Ishouldeatmore.FAT_SWORD.get(), Ishouldeatmore.MUSCLE_SWORD.get(), Ishouldeatmore.BONE_SWORD.get(),
                        Ishouldeatmore.ARM_SWORD.get(), Ishouldeatmore.GOLDEN_ARM_SWORD.get());

        // Slot-specific armor (for Respiration on helmet, Depth Strider on boots, etc.)
        tag(ENCHANTABLE_HEAD_ARMOR)
                .add(Ishouldeatmore.FAT_HELMET.get(), Ishouldeatmore.MUSCLE_HELMET.get(), Ishouldeatmore.BONE_HELMET.get(),
                        Ishouldeatmore.ARM_HELMET.get(), Ishouldeatmore.GOLDEN_ARM_HELMET.get());
        tag(ENCHANTABLE_CHEST_ARMOR)
                .add(Ishouldeatmore.FAT_CHESTPLATE.get(), Ishouldeatmore.MUSCLE_CHESTPLATE.get(), Ishouldeatmore.BONE_CHESTPLATE.get(),
                        Ishouldeatmore.ARM_CHESTPLATE.get(), Ishouldeatmore.GOLDEN_ARM_CHESTPLATE.get());
        tag(ENCHANTABLE_LEG_ARMOR)
                .add(Ishouldeatmore.FAT_LEGGINGS.get(), Ishouldeatmore.MUSCLE_LEGGINGS.get(), Ishouldeatmore.BONE_LEGGINGS.get(),
                        Ishouldeatmore.ARM_LEGGINGS.get(), Ishouldeatmore.GOLDEN_ARM_LEGGINGS.get());
        tag(ENCHANTABLE_FOOT_ARMOR)
                .add(Ishouldeatmore.FAT_BOOTS.get(), Ishouldeatmore.MUSCLE_BOOTS.get(), Ishouldeatmore.BONE_BOOTS.get(),
                        Ishouldeatmore.ARM_BOOTS.get(), Ishouldeatmore.GOLDEN_ARM_BOOTS.get());

        // Sword-specific
        tag(ENCHANTABLE_FIRE_ASPECT)
                .add(Ishouldeatmore.FAT_SWORD.get(), Ishouldeatmore.MUSCLE_SWORD.get(), Ishouldeatmore.BONE_SWORD.get(),
                        Ishouldeatmore.ARM_SWORD.get(), Ishouldeatmore.GOLDEN_ARM_SWORD.get());
        tag(ENCHANTABLE_SHARP_WEAPON)
                .add(Ishouldeatmore.FAT_SWORD.get(), Ishouldeatmore.MUSCLE_SWORD.get(), Ishouldeatmore.BONE_SWORD.get(),
                        Ishouldeatmore.ARM_SWORD.get(), Ishouldeatmore.GOLDEN_ARM_SWORD.get());
    }
}
