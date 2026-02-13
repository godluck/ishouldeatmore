package com.gdlk.ishouldeatmore.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Tiers;

public class FatSword extends FoodSword{

    public FatSword(Properties properties) {
        super(Tiers.WOOD, properties);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
        return super.getAttackDamageBonus(target, damage, damageSource) * 1;
    }
}
