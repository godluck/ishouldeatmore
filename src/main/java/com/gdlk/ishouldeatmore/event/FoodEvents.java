package com.gdlk.ishouldeatmore.event;

import com.gdlk.ishouldeatmore.data.LightningDamageScheduler;
import com.gdlk.ishouldeatmore.network.FoodDataSync;
import com.gdlk.ishouldeatmore.network.SyncFoodEatenPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FoodEvents {

    private final LightningDamageScheduler lightningScheduler;

    public FoodEvents(LightningDamageScheduler lightningScheduler) {
        this.lightningScheduler = lightningScheduler;
    }

    @SubscribeEvent
    public void onFoodEatStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack food = event.getItem();
            FoodProperties foodProperties = food.getFoodProperties(null);
            if (foodProperties != null && foodProperties.nutrition() > 0) {
                event.setDuration(0);
            }
        }
    }

    @SubscribeEvent
    public void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack food = event.getItem();
            FoodProperties foodProperties = food.getFoodProperties(null);
            if (foodProperties == null || foodProperties.nutrition() <= 0) {
                return;
            }
            FoodData foodData = player.getFoodData();
            if (!(foodData instanceof FoodDataSync foodDataSync)) {
                return;
            }
            int stage = foodDataSync.ishouldeatmore$getFoodLevelStage();
            if (Math.log10(foodData.getFoodLevel()) != Math.max(2, stage + 1)) {
                return;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer,
                        new SyncFoodEatenPayload(foodDataSync.ishouldeatmore$getFoodEaten()));
                lightningScheduler.scheduleForStageAdvance(serverPlayer, stage, stage + 1);
            }
        }
    }
}
