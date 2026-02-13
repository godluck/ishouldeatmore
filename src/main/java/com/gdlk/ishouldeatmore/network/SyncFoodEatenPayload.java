package com.gdlk.ishouldeatmore.network;

import com.gdlk.ishouldeatmore.Ishouldeatmore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record SyncFoodEatenPayload(List<String> foodEaten) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncFoodEatenPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Ishouldeatmore.MODID, "sync_food_eaten"));

    public static final StreamCodec<ByteBuf, SyncFoodEatenPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                    SyncFoodEatenPayload::foodEaten,
                    SyncFoodEatenPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
