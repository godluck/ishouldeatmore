package com.gdlk.ishouldeatmore.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.*;


public class FoodSword extends SwordItem {
    public FoodSword(Properties properties) {
        super(Tiers.WOOD, properties);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource){
        if (damageSource.getEntity() instanceof Player player){
            FoodData foodData = player.getFoodData();
            double mag = Math.log10(foodData.getFoodLevel());
            if (mag < 1) {
                return 0;
            }
            return (float) (mag * mag);
        }
        return 0;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            FoodData foodData = player.getFoodData();
            float saturationLevel = foodData.getSaturationLevel();
            int foodLevel = foodData.getFoodLevel();
            double mag = Math.log10(foodLevel);
            if (mag < 1) {
                return;
            }

            if (saturationLevel > mag){
                foodData.setSaturation((float) (saturationLevel - mag));
            } else if (saturationLevel + foodLevel > mag){
                foodData.setSaturation(0);
                foodData.setFoodLevel((int)Math.round(foodLevel + saturationLevel - mag));
            } else {
                foodData.setFoodLevel(0);
                foodData.setSaturation(0);
            }
        }
    }
}
