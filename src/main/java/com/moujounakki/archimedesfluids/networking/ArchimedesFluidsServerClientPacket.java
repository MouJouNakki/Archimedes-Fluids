package com.moujounakki.archimedesfluids.networking;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class ArchimedesFluidsServerClientPacket extends ArchimedesFluidsPacket {
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(ctx))
        );
        ctx.get().setPacketHandled(true);
    }
}
