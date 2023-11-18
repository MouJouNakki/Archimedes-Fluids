package com.moujounakki.archimedesfluids.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.IndexedMessageCodec;

import java.util.function.Supplier;

public abstract class ArchimedesFluidsPacket {
    public abstract void encode(FriendlyByteBuf buf);
    // Must be defined in subclasses:
    // public static (subclass) decode(FriendlyByteBuf buf)
    public abstract void handle(Supplier<NetworkEvent.Context> ctx);
    public abstract void handlePacket(Supplier<NetworkEvent.Context> ctx);
}
