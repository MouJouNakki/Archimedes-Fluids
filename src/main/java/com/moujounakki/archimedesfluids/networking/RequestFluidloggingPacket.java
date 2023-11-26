package com.moujounakki.archimedesfluids.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestFluidloggingPacket extends ArchimedesFluidsClientServerPacket {
    public final ChunkPos chunkPos;
    public RequestFluidloggingPacket(ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
    }
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(chunkPos);
    }
    @SuppressWarnings("unused")
    public static RequestFluidloggingPacket decode(FriendlyByteBuf buf) {
        return new RequestFluidloggingPacket(buf.readChunkPos());
    }
    @Override
    public void handlePacket(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if(sender == null)
            return;
        ChunkAccess chunk = sender.getLevel().getChunk(chunkPos.getWorldPosition());
    }
}
