package com.gdlk.ishouldeatmore.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.gdlk.ishouldeatmore.Ishouldeatmore.LOGGER;

@Mixin(value = {FoodData.class}, priority = 1001)
public abstract class FoodDataMixin {
    private int foodLevel;
    private float saturationLevel;


    public FoodDataMixin() {
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void add(int foodLevel, float saturationLevel, CallbackInfo ci) {
        double magnitude = Math.max(Math.log10(this.foodLevel), 1);
        if (foodLevel < magnitude) {
            ci.cancel();
            return;
        }

        double foodQuality= Math.log10(foodLevel) + 1;
        this.foodLevel = (int)Math.round((double) foodLevel / magnitude * foodQuality) + this.foodLevel;

        if (saturationLevel + this.saturationLevel > this.foodLevel) {
            float diff = saturationLevel + this.saturationLevel - this.foodLevel;
            int diffMagnitude = Math.max(Math.getExponent(diff), 1);
            float saturationToAdd = (float) (saturationLevel / diffMagnitude * foodQuality);
            this.saturationLevel += saturationToAdd;
        } else {
            this.saturationLevel = (float) (saturationLevel * foodQuality + this.saturationLevel);
        }
        ci.cancel();
    }

    @Inject(method = "needsFood", at = @At("HEAD"), cancellable = true)
    private void needsFood(CallbackInfoReturnable<Boolean> cir) {
        LOGGER.info("needsFood");

        cir.setReturnValue(true);
    }
}