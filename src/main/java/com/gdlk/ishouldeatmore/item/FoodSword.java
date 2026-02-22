package com.gdlk.ishouldeatmore.item;

import com.gdlk.ishouldeatmore.network.FoodDataSync;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.*;

public class FoodSword extends SwordItem {

    public FoodSword(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
        if (damageSource.getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof FoodSword)) return 0;
            FoodData foodData = player.getFoodData();
            if (foodData instanceof FoodDataSync foodDataSync){
                int stage = foodDataSync.ishouldeatmore$getFoodLevelStage();
                return stage * stage;
            }
        }
        return 0;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            FoodData foodData = player.getFoodData();
            if (foodData instanceof FoodDataSync foodDataSync){
                int stage = foodDataSync.ishouldeatmore$getFoodLevelStage();
                float saturationLevel = foodData.getSaturationLevel();
                int foodLevel = foodData.getFoodLevel();
                if (stage < 1) return;

                if (saturationLevel > stage) {
                    foodData.setSaturation(saturationLevel - stage);
                } else if (saturationLevel + foodLevel > stage) {
                    foodData.setSaturation(0);
                    foodData.setFoodLevel(Math.round(foodLevel + saturationLevel - stage));
                } else {
                    foodData.setFoodLevel(0);
                    foodData.setSaturation(0);
                }
                stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
            }
        }
    }
}
