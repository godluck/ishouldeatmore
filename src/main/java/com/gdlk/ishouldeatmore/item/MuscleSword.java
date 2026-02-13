package com.gdlk.ishouldeatmore.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

public class MuscleSword extends FoodSword{

    public MuscleSword(Properties properties) {
        super(Tiers.STONE, properties);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
        return super.getAttackDamageBonus(target, damage, damageSource) * 2;
    }
}
