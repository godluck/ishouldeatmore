package com.gdlk.ishouldeatmore.event;

import com.gdlk.ishouldeatmore.Ishouldeatmore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class ItemTooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        String id = stack.getDescriptionId();
        if (!id.startsWith("item." + Ishouldeatmore.MODID + ".")) {
            return;
        }
        event.getToolTip().add(Component.translatable(id + ".tooltip").withStyle(ChatFormatting.GRAY));
        if (stack.is(Ishouldeatmore.FAT_SWORD.get()) || stack.is(Ishouldeatmore.FAT_HELMET.get())
                || stack.is(Ishouldeatmore.FAT_CHESTPLATE.get()) || stack.is(Ishouldeatmore.FAT_LEGGINGS.get())
                || stack.is(Ishouldeatmore.FAT_BOOTS.get())) {
            event.getToolTip().add(
                    Component.translatable("item.ishouldeatmore.craft_tip.fat_to_muscle")
                            .withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}
