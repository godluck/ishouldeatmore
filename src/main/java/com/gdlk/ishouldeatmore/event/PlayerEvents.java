package com.gdlk.ishouldeatmore.event;

import com.gdlk.ishouldeatmore.Ishouldeatmore;
import com.gdlk.ishouldeatmore.item.FatSword;
import com.gdlk.ishouldeatmore.item.FoodArmor;
import com.gdlk.ishouldeatmore.network.FoodDataSync;
import com.gdlk.ishouldeatmore.network.FoodLevelStagePayload;
import com.gdlk.ishouldeatmore.network.SyncFoodEatenPayload;
import com.gdlk.ishouldeatmore.util.FoodStageHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PlayerEvents {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer && player.getFoodData() instanceof FoodDataSync sync) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncFoodEatenPayload(sync.ishouldeatmore$getFoodEaten()));
            PacketDistributor.sendToPlayer(serverPlayer, new FoodLevelStagePayload(sync.ishouldeatmore$getFoodLevelStage()));
        }
    }

    @SubscribeEvent
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (FoodArmor.countFoodArmorPieces(player) == 0) {
            return;
        }
        if (FoodStageHelper.getStage(player) < 2) {
            return;
        }
        float saturationLevel = player.getFoodData().getSaturationLevel();
        int foodLevel = player.getFoodData().getFoodLevel();
        float originalDamage = event.getAmount();
        float effectiveMaxReduction = FoodArmor.getEffectiveMaxReduction(player);
        float damageToAbsorb = originalDamage * effectiveMaxReduction;
        float pool = saturationLevel + foodLevel;
        float actuallyAbsorbed = Math.min(damageToAbsorb, pool);
        if (actuallyAbsorbed <= 0) {
            return;
        }
        if (saturationLevel >= actuallyAbsorbed) {
            player.getFoodData().setSaturation(saturationLevel - actuallyAbsorbed);
        } else if (saturationLevel + foodLevel <= actuallyAbsorbed) {
            player.getFoodData().setFoodLevel(0);
            player.getFoodData().setSaturation(0);
        } else {
            player.getFoodData().setSaturation(0);
            player.getFoodData().setFoodLevel(
                    Math.max(0, (int) Math.floor(foodLevel + saturationLevel - actuallyAbsorbed)));
        }
        event.setAmount(originalDamage - actuallyAbsorbed);
    }

    @SubscribeEvent
    public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        ItemStack original = event.getOriginal();
        Player player = event.getEntity();
        if (original.getItem() instanceof FatSword) {
            ItemStack muscle = new ItemStack(Ishouldeatmore.MUSCLE_SWORD.asItem(), 1);
            if (event.getHand() != null) {
                player.setItemInHand(event.getHand(), muscle);
            }
        }
    }

    @SubscribeEvent
    public void onArmorHurt(ArmorHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }
        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        for (EquipmentSlot slot : armorSlots) {
            ItemStack armorStack = player.getItemBySlot(slot);
            if (armorStack.isEmpty() || !(armorStack.getItem() instanceof FoodArmor)) {
                continue;
            }
            if (armorStack.getMaxDamage() - armorStack.getDamageValue() > 1) {
                continue;
            }
            Item replacement = null;
            if (armorStack.is(Ishouldeatmore.FAT_HELMET.get())) {
                replacement = Ishouldeatmore.MUSCLE_HELMET.get();
            } else if (armorStack.is(Ishouldeatmore.FAT_CHESTPLATE.get())) {
                replacement = Ishouldeatmore.MUSCLE_CHESTPLATE.get();
            } else if (armorStack.is(Ishouldeatmore.FAT_LEGGINGS.get())) {
                replacement = Ishouldeatmore.MUSCLE_LEGGINGS.get();
            } else if (armorStack.is(Ishouldeatmore.FAT_BOOTS.get())) {
                replacement = Ishouldeatmore.MUSCLE_BOOTS.get();
            }
            if (replacement != null) {
                player.setItemSlot(slot, new ItemStack(replacement, 1));
            }
        }
    }
}
