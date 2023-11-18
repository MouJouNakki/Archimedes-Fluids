package com.moujounakki.archimedesfluids.networking;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestFluidloggingPacket extends ArchimedesFluidsClientServerPacket {
    public final ChunkAccess chunkAccess;
    public final ClientLevel level;
    public RequestFluidloggingPacket(ChunkAccess chunkAccess, ClientLevel level) {
        this.chunkAccess = chunkAccess;
        this.level = level;
    }
    @Override
    public void encode(FriendlyByteBuf buf) {

    }
    @SuppressWarnings("unused")
    public static RequestFluidloggingPacket decode(FriendlyByteBuf buf) {
        return new RequestFluidloggingPacket();
    }
    @Override
    public void handlePacket(Supplier<NetworkEvent.Context> ctx) {
        
    }
}
