package com.gdlk.ishouldeatmore.network;

import com.gdlk.ishouldeatmore.Ishouldeatmore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FoodLevelStagePayload(int stage) implements CustomPacketPayload {

    public static final Type<FoodLevelStagePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ishouldeatmore.MODID, "food_level_stage"));

    public static final StreamCodec<ByteBuf, FoodLevelStagePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, FoodLevelStagePayload::stage, FoodLevelStagePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
