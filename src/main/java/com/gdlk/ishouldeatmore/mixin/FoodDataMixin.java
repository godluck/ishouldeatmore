package com.gdlk.ishouldeatmore.mixin;

import com.gdlk.ishouldeatmore.network.FoodDataSync;
import com.gdlk.ishouldeatmore.network.FoodLevelStagePayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.food.FoodData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;
import java.util.*;

import static com.gdlk.ishouldeatmore.Ishouldeatmore.LOGGER;

@Mixin(value = {FoodData.class}, priority = 1001)
public abstract class FoodDataMixin implements FoodDataSync {
    private int foodLevel;
    private float saturationLevel;
    private float exhaustionLevel;
    private int tickTimer;
    private Queue<String> foodEaten;
    private int foodLevelStage;

    public FoodDataMixin() {
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void add(int foodLevel, float saturationLevel, CallbackInfo ci) {
        // Track foodLevel & saturationLevel combination
        String foodKey = """
                %s,%.2f""".formatted(foodLevel, saturationLevel);
        double magnitude = Math.max(Math.log10(this.foodLevel), 1);
        int magInt = (int) magnitude + 5;
        if (this.foodEaten == null) {
            this.foodEaten = new ArrayDeque<>(magInt);
        }
        // Add new record
        this.foodEaten.add(foodKey);
        // Remove items exceeding magInt
        if (this.foodEaten.size() > magInt) {
            for (int i = magInt; i < this.foodEaten.size(); ++i) {
                this.foodEaten.poll();
            }
        }
        int types = new HashSet<>(this.foodEaten).size();
        // Scale the foodLevel by (1,2] base on record count
        double varietyScale = (double) types / magInt + 1;

        double foodQuality= Math.log10(foodLevel) + 1;
        this.foodLevel = (int)Math.round((double) foodLevel / magnitude * foodQuality * varietyScale) + this.foodLevel;
        if (saturationLevel + this.saturationLevel > this.foodLevel) {
            float diff = saturationLevel + this.saturationLevel - this.foodLevel;
            int diffMagnitude = (int) Math.max(Math.log10(diff), 1);
            float saturationToAdd = (float) (saturationLevel / diffMagnitude * foodQuality * varietyScale);
            this.saturationLevel += saturationToAdd;
        } else {
            this.saturationLevel = (float) (saturationLevel * foodQuality * varietyScale + this.saturationLevel);
        }
        if (Math.log10(this.foodLevel) >= 1 && this.foodLevelStage < 1){
            this.foodLevelStage = 1;
        }
        int foodLevelStageLimit = Math.max(2, this.foodLevelStage + 1);
        if (Math.log10(this.foodLevel) > foodLevelStageLimit){
            this.foodLevel = (int) Math.pow(10, foodLevelStageLimit);
        }

        ci.cancel();
    }

    @Inject(method = "needsFood", at = @At("HEAD"), cancellable = true)
    private void needsFood(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Override
    public List<String> ishouldeatmore$getFoodEaten() {
        if (this.foodEaten == null) {
            return List.of();
        }
        return new ArrayList<>(this.foodEaten);
    }

    @Override
    public void ishouldeatmore$setFoodEaten(List<String> foodEaten) {
        this.foodEaten = foodEaten == null ? new ArrayDeque<>() : new ArrayDeque<>(foodEaten);
    }

    @Override
    public int ishouldeatmore$getFoodLevelStage(){
        return this.foodLevelStage;
    }

    @Override
    public void ishouldeatmore$setFoodLevelStage(int foodLevelStage){
        this.foodLevelStage = foodLevelStage;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"), cancellable = true)
    public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        if (compoundTag.contains("foodLevel", 99)) {
            this.foodLevel = compoundTag.getInt("foodLevel");
            this.tickTimer = compoundTag.getInt("foodTickTimer");
            this.saturationLevel = compoundTag.getFloat("foodSaturationLevel");
            this.exhaustionLevel = compoundTag.getFloat("foodExhaustionLevel");
            this.foodLevelStage = compoundTag.getInt("foodLevelStage");
            boolean hasFoodEaten = compoundTag.contains("foodEaten");
            if (hasFoodEaten) {
                byte[] foodEaten = compoundTag.getByteArray("foodEaten");
                ByteArrayInputStream byteIn = new ByteArrayInputStream(foodEaten);
                ObjectInputStream in = null;
                try {
                    in = new ObjectInputStream(byteIn);
                    this.foodEaten = (Queue<String>) in.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"), cancellable = true)
    public void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putInt("foodLevel", this.foodLevel);
        compoundTag.putInt("foodTickTimer", this.tickTimer);
        compoundTag.putFloat("foodSaturationLevel", this.saturationLevel);
        compoundTag.putFloat("foodExhaustionLevel", this.exhaustionLevel);
        compoundTag.putInt("foodLevelStage", this.foodLevelStage);
        if (this.foodEaten != null && !this.foodEaten.isEmpty()) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try {
                ObjectOutputStream out = new ObjectOutputStream(byteOut);
                out.writeObject(this.foodEaten);
                out.flush();
                compoundTag.putByteArray("foodEaten", byteOut.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ci.cancel();
    }
}