package com.moujounakki.archimedesfluids.mixins;

import net.minecraft.block.WallBlock;
import com.moujounakki.archimedesfluids.ArchimedesFluids;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StateManager.Builder.class)
@SuppressWarnings("unused")
public abstract class MixinStateManagerBuilder<O, S extends State<O, S>> {
    @Shadow
    private O owner;
    @Shadow
    public abstract StateManager.Builder<O, S> add(Property<?>... p_61105_);
    @Inject(method = "validate", at = @At("HEAD"))
    private <T extends Comparable<T>> void onValidate(Property<T> property, CallbackInfo callbackInfo) {
        if(owner instanceof WallBlock)
            return;
        if(property == Properties.WATERLOGGED)
            add(ArchimedesFluids.FLUIDLOGGED, ArchimedesFluids.FLUID_LEVEL);
    }
}
