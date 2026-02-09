package com.gdlk.ishouldeatmore.network;

import com.gdlk.ishouldeatmore.Ishouldeatmore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageSaturationSync(float saturationLevel) implements CustomPacketPayload {
    public static final Type<MessageSaturationSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ishouldeatmore.MODID, "saturation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSaturationSync> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            MessageSaturationSync::saturationLevel,
            MessageSaturationSync::new
    );

    public static void handle(final MessageSaturationSync message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ctx.player().getFoodData().setSaturation(message.saturationLevel);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}