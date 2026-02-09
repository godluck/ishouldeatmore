package com.gdlk.ishouldeatmore.mixin;

import net.minecraft.util.Mth;
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
        this.foodLevel = foodLevel + this.foodLevel;
        this.saturationLevel = saturationLevel + this.saturationLevel;
        ci.cancel();
    }

    @Inject(method = "needsFood", at = @At("HEAD"), cancellable = true)
    private void needsFood(CallbackInfoReturnable<Boolean> cir) {
        LOGGER.info("needsFood");

        cir.setReturnValue(true);
    }
}