package com.moujounakki.archimedesfluids.networking;

import com.machinezoo.noexception.throwing.ThrowingBiConsumer;
import com.moujounakki.archimedesfluids.ArchimedesFluids;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class ArchimedesFluidsPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int nextIndex = 0;
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ArchimedesFluids.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    @SuppressWarnings("unchecked")
    public static <T extends ArchimedesFluidsPacket> void register(Class<T> packet) {
        INSTANCE.registerMessage(nextIndex++, packet, T::encode, (FriendlyByteBuf buf) -> {
            try {
                Method method = packet.getMethod("decode", FriendlyByteBuf.class);
                if(method.getReturnType() != packet)
                    throw new RuntimeException("Static method 'decode' has wrong return type in class %s. ".formatted(packet.getName()));
                return (T)method.invoke(null, buf);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException("Static method 'decode' is not defined in class %s. ".formatted(packet.getName()));
            }
            catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, T::handle);
    }
}
