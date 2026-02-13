package com.gdlk.ishouldeatmore.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * Armor that reduces incoming damage by consuming the player's food level and saturation.
 * Max reduction per full set depends on tier: fat 30%, muscle 50%, bone 70%, arm 85%, golden_arm 100%.
 */
public class FoodArmor extends ArmorItem {

    public FoodArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /**
     * Max damage reduction (0–1) for a full set of this material. Fat=30%, Golden Arm=100%.
     */
    public static float getMaxReductionForMaterial(Holder<ArmorMaterial> material) {
        ResourceLocation id = BuiltInRegistries.ARMOR_MATERIAL.getKey(material.value());
        if (id == null) return 0.3f;
        String path = id.getPath();
        return switch (path) {
            case "leather" -> 0.30f;
            case "chainmail" -> 0.50f;
            case "iron" -> 0.70f;
            case "diamond" -> 0.85f;
            case "netherite" -> 1.00f;
            default -> 0.30f;
        };
    }

    /**
     * Effective max reduction (0–1) for the current set: average of each worn piece's tier.
     * Full set fat = 30%, full set golden_arm = 100%; mixed sets scale accordingly.
     */
    public static float getEffectiveMaxReduction(Player player) {
        EquipmentSlot[] armorSlots = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
        float sum = 0f;
        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof FoodArmor fa) {
                sum += getMaxReductionForMaterial(fa.getMaterial());
            }
        }
        return sum / 4f;
    }

    /**
     * Returns true if the player is wearing any food leggings in the legs slot.
     */
    public static boolean isWearingFoodLeggings(Player player) {
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        return !legs.isEmpty() && legs.getItem() instanceof FoodArmor fa && fa.getType() == Type.LEGGINGS;
    }

    /**
     * Returns the number of FoodArmor pieces equipped (0–4).
     */
    public static int countFoodArmorPieces(Player player) {
        EquipmentSlot[] armorSlots = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
        int count = 0;
        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof FoodArmor) {
                count++;
            }
        }
        return count;
    }
}
