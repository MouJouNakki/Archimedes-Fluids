package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IMixinFlowingFluid {
    void changeFluid(Level level, BlockPos pos, int amount);
}
