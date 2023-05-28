package com.moujounakki.fluidmotionoverhaul;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;

public interface IMixinFlowingFluid {
    boolean checkForFluidInWay(LevelAccessor level, BlockPos pos, FluidState state);
    void moveFluidInWay(LevelAccessor level, BlockPos pos, FluidState state);
}
