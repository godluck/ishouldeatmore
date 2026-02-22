package com.gdlk.ishouldeatmore.client;

import com.gdlk.ishouldeatmore.network.FoodDataSync;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import static com.gdlk.ishouldeatmore.Ishouldeatmore.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class FoodHudRenderer {

    private static final ResourceLocation FOOD_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation FOOD_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_half");

    private FoodHudRenderer() {}

    @SubscribeEvent
    public static void onRenderFoodLayer(RenderGuiLayerEvent.Pre event) {
        if (event.getName() != VanillaGuiLayers.FOOD_LEVEL) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        int saturationLevel = (int) foodData.getSaturationLevel();
        int foodLevelStage = foodData instanceof FoodDataSync sync
                ? sync.ishouldeatmore$getFoodLevelStage()
                : -1;

        int y = mc.getWindow().getGuiScaledHeight() - mc.gui.rightHeight - 10;
        int x = mc.getWindow().getGuiScaledWidth() / 2 + 10;

        RenderSystem.enableBlend();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int iconCount = Math.max(1, foodLevelStage);
        for (int i = 0; i < iconCount; i++) {
            guiGraphics.blitSprite(FOOD_EMPTY_SPRITE, x + 8 * i, y, 9, 9);
            guiGraphics.blitSprite(FOOD_HALF_SPRITE, x + 8 * i, y, 9, 9);
        }
        String countString = "*%d (+%d)".formatted(foodLevel, saturationLevel);
        guiGraphics.drawString(mc.gui.getFont(), Component.literal(countString), x + 8 * iconCount, y, 0xffffff);
        RenderSystem.disableBlend();
    }
}
