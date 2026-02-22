package com.gdlk.ishouldeatmore.network;

import com.gdlk.ishouldeatmore.item.FoodArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers play payload handlers for client-server sync and movement (air jump, dash).
 */
public final class PayloadHandlers {

    private PayloadHandlers() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(SyncFoodEatenPayload.TYPE, SyncFoodEatenPayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                var player = Minecraft.getInstance().player;
                if (player != null && player.getFoodData() instanceof FoodDataSync sync) {
                    sync.ishouldeatmore$setFoodEaten(payload.foodEaten());
                }
            });
        });

        registrar.playToClient(FoodLevelStagePayload.TYPE, FoodLevelStagePayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                var player = Minecraft.getInstance().player;
                if (player != null && player.getFoodData() instanceof FoodDataSync sync) {
                    sync.ishouldeatmore$setFoodLevelStage(payload.stage());
                }
            });
        });

        registrar.playToServer(AirJumpPayload.TYPE, AirJumpPayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!FoodArmor.isWearingFoodLeggings(player)) {
                        return;
                    }
                    Vec3 motion = player.getDeltaMovement();
                    float consumed = 0.01f;
                    if (motion.y < 0.05 && payload.delta() == 0) {
                        player.setDeltaMovement(motion.x, 0, motion.z);
                    }
                    if (payload.delta() > 0) {
                        player.setDeltaMovement(new Vec3(motion.x, payload.delta(), motion.z));
                        consumed = 1f;
                    }
                    float saturationLevel = player.getFoodData().getSaturationLevel();
                    if (saturationLevel >= consumed) {
                        player.getFoodData().setSaturation(saturationLevel - consumed);
                    } else {
                        player.getFoodData().setSaturation(0);
                    }
                }
            });
        });

        registrar.playToServer(DashPayload.TYPE, DashPayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (FoodArmor.isWearingFoodLeggings(player)) {
                        float consumed = 1f;
                        player.addDeltaMovement(player.getLookAngle().normalize().scale(0.7));
                        float saturationLevel = player.getFoodData().getSaturationLevel();
                        if (saturationLevel >= consumed) {
                            player.getFoodData().setSaturation(saturationLevel - consumed);
                        } else {
                            player.getFoodData().setSaturation(0);
                        }
                    }
                }
            });
        });
    }
}
