package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

public interface IMixinFlowingFluid {
    void changeFluid(Level level, BlockPos pos, int amount);
    void runTick(Level level, BlockPos pos, FluidState state);
}
