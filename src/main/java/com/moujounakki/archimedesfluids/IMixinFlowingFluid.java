package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;

public interface IMixinFlowingFluid {
    void evaporateFluid(LevelAccessor level, BlockPos pos, FluidState state);
    boolean rainFluid(LevelAccessor level, BlockPos pos);
}
