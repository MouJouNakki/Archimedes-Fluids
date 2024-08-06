package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.ArchimedesFluids;
import com.moujounakki.archimedesfluids.StateHolderNeighbourState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(StateHolder.class)
public abstract class MixinStateHolder<O,S> {
    @Shadow
    public abstract <T extends Comparable<T>> boolean hasProperty(Property<T> p_61139_);
    @Shadow
    public abstract<T extends Comparable<T>> T getValue(Property<T> p_61144_);
    @Shadow
    public abstract void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> p_61134_);
    private StateHolderNeighbourState populateNeighboursState = StateHolderNeighbourState.NORMAL;
    private Map<Map<Property<?>, Comparable<?>>, S> populateNeighboursMap;
    @Inject(method = "populateNeighbours", at = @At("HEAD"), cancellable = true)
    private void onPopulateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> p_61134_, CallbackInfo callbackInfo) {
        if (populateNeighboursState != StateHolderNeighbourState.POPULATE_NOW && hasProperty(ArchimedesFluids.FLUID_LEVEL)) {// && getValue(ArchimedesFluids.FLUID_LEVEL) != 0) {
            populateNeighboursState = StateHolderNeighbourState.DELAYED;
            populateNeighboursMap = p_61134_;
            callbackInfo.cancel();
        }
    }
    @Inject(method = "setValue", at = @At("HEAD"))
    private <T extends Comparable<T>, V extends T> void onSetValue(Property<T> p_61125_, V p_61126_, CallbackInfoReturnable<S> callbackInfoReturnable) {
        if (populateNeighboursState == StateHolderNeighbourState.DELAYED) {
            populateNeighboursState = StateHolderNeighbourState.POPULATE_NOW;
            populateNeighbours(populateNeighboursMap);
        }
    }
}
