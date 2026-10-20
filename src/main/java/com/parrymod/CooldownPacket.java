package com.parrymod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent server -> client so the HUD can show the parry cooldown. */
public class CooldownPacket {
    private final int ticks;

    public CooldownPacket(int ticks) {
        this.ticks = ticks;
    }

    public static void encode(CooldownPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.ticks);
    }

    public static CooldownPacket decode(FriendlyByteBuf buf) {
        return new CooldownPacket(buf.readVarInt());
    }

    public static void handle(CooldownPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        int ticks = msg.ticks;
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.setCooldown(ticks)));
        context.setPacketHandled(true);
    }
}
