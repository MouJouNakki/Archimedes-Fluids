package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

public class FluidTick {
    public final IMixinFlowingFluid fluid;
    public final Level level;
    public final BlockPos pos;
    public final FluidState state;
    public FluidTick(IMixinFlowingFluid fluid, Level level, BlockPos pos, FluidState state) {
        this.fluid = fluid;
        this.level = level;
        this.pos = pos;
        this.state = state;
    }
    public void tick() {
        fluid.runTick(level, pos, state);
    }
}
