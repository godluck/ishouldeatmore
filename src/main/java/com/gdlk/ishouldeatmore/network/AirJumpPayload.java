package com.gdlk.ishouldeatmore.network;

import com.gdlk.ishouldeatmore.Ishouldeatmore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AirJumpPayload(double delta) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AirJumpPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Ishouldeatmore.MODID, "air_jump"));

    public static final StreamCodec<ByteBuf, AirJumpPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.DOUBLE, AirJumpPayload::delta, AirJumpPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
