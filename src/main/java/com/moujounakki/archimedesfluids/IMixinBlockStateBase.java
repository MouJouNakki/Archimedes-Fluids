package com.moujounakki.archimedesfluids;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public interface IMixinBlockStateBase {
    FluidState getFluidloggingState();
    void setFluidloggingState(FluidState state);
}
