package com.gdlk.ishouldeatmore.network;

import java.util.List;

/**
 * Interface for syncing custom food data (e.g. foodEaten) from server to client.
 * Implemented by FoodDataMixin on FoodData.
 */
public interface FoodDataSync {
    List<String> ishouldeatmore$getFoodEaten();
    void ishouldeatmore$setFoodEaten(List<String> foodEaten);
}
