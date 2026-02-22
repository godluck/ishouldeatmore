package com.gdlk.ishouldeatmore.util;

import com.gdlk.ishouldeatmore.network.FoodDataSync;
import net.minecraft.world.entity.player.Player;

/**
 * Common helpers for food level stage (used by both server and client).
 */
public final class FoodStageHelper {

    private FoodStageHelper() {}

    /**
     * Returns the current food level stage for the player, or 0 if not available.
     */
    public static int getStage(Player player) {
        if (player == null) {
            return 0;
        }
        if (player.getFoodData() instanceof FoodDataSync sync) {
            return sync.ishouldeatmore$getFoodLevelStage();
        }
        return 0;
    }
}
