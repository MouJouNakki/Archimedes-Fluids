package com.moujounakki.archimedesfluids.networking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class ArchimedesFluidsClientServerPacket extends ArchimedesFluidsPacket {
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handlePacket(ctx);
        });
        ctx.get().setPacketHandled(true);
    }
}
