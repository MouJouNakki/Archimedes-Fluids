package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.Level;

public interface IMixinFlowingFluid {
    boolean checkForFluidInWay(LevelAccessor level, BlockPos pos, FluidState state);
    void moveFluidInWay(LevelAccessor level, BlockPos pos, FluidState state);
    void changeFluid(Level level, BlockPos pos, int amount);
}
