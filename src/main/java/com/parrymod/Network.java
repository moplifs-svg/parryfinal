package com.parrymod;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Network {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ParryMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, ParryPacket.class,
                ParryPacket::encode, ParryPacket::decode, ParryPacket::handle);
        CHANNEL.registerMessage(1, CooldownPacket.class,
                CooldownPacket::encode, CooldownPacket::decode, CooldownPacket::handle);
    }
}
