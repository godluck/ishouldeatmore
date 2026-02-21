package com.gdlk.ishouldeatmore.network;

import com.gdlk.ishouldeatmore.Ishouldeatmore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DashPayload(double delta) implements CustomPacketPayload {

    public static final Type<DashPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ishouldeatmore.MODID, "dash"));

    public static final StreamCodec<ByteBuf, DashPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.DOUBLE, DashPayload::delta, DashPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
