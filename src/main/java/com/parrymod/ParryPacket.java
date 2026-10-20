package com.parrymod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent client -> server when the parry key is pressed. */
public class ParryPacket {
    public static void encode(ParryPacket msg, FriendlyByteBuf buf) {
    }

    public static ParryPacket decode(FriendlyByteBuf buf) {
        return new ParryPacket();
    }

    public static void handle(ParryPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ParryHandler.startParry(player);
            }
        });
        context.setPacketHandled(true);
    }
}
