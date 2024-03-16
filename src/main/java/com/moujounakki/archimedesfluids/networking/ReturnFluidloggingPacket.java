package com.moujounakki.archimedesfluids.networking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReturnFluidloggingPacket extends ArchimedesFluidsServerClientPacket {
//    public final LevelAccessor level;
//
//    public ReturnFluidloggingPacket(LevelAccessor level) {
//        this.level = level;
//    }
    public ReturnFluidloggingPacket() {}

    @Override
    public void encode(FriendlyByteBuf buf) {

    }
    @SuppressWarnings("unused")
    public static ReturnFluidloggingPacket decode(FriendlyByteBuf buf) {
        return new ReturnFluidloggingPacket();
    }
    @Override
    public void handlePacket(Supplier<NetworkEvent.Context> ctx) {

    }
}
