package com.gdlk.ishouldeatmore.client;

import com.gdlk.ishouldeatmore.item.FoodArmor;
import com.gdlk.ishouldeatmore.network.AirJumpPayload;
import com.gdlk.ishouldeatmore.network.DashPayload;
import com.gdlk.ishouldeatmore.util.FoodStageHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static com.gdlk.ishouldeatmore.Ishouldeatmore.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ClientInputHandler {

    private ClientInputHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.keyJump.isDown() || !FoodArmor.isWearingFoodLeggings(player)) {
            return;
        }
        if (FoodStageHelper.getStage(player) < 3) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0.1) {
            player.setDeltaMovement(motion.x, 0, motion.z);
        }
        PacketDistributor.sendToServer(new AirJumpPayload(0));
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        KeyMapping keyJump = mc.options.keyJump;
        if (isKeyPressed(event, keyJump, 4)) {
            keyJump.consumeClick();
            LocalPlayer player = mc.player;
            if (player != null && !player.onGround()) {
                Vec3 currentDelta = player.getDeltaMovement();
                player.setDeltaMovement(new Vec3(currentDelta.x, 0.7, currentDelta.z));
                PacketDistributor.sendToServer(new AirJumpPayload(0.7));
            }
            return;
        }

        KeyMapping keySprint = mc.options.keySprint;
        if (isKeyPressed(event, keySprint, 5)) {
            LocalPlayer player = mc.player;
            if (player == null || player.onGround()) {
                return;
            }
            keySprint.consumeClick();
            player.addDeltaMovement(player.getLookAngle().normalize().scale(0.7));
            PacketDistributor.sendToServer(new DashPayload(0.7));
        }
    }

    private static boolean isKeyPressed(InputEvent.Key event, KeyMapping key, int foodLevelStageLimit) {
        if (!key.matches(event.getKey(), event.getScanCode())) {
            return false;
        }
        if (!isInGame()) {
            return false;
        }
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return false;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isShiftKeyDown()) {
            return false;
        }
        if (FoodStageHelper.getStage(player) < foodLevelStageLimit) {
            return false;
        }
        return true;
    }

    private static boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getOverlay() != null) {
            return false;
        }
        if (mc.screen != null) {
            return false;
        }
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        return mc.isWindowActive();
    }
}
