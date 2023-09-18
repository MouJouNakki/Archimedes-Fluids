package com.moujounakki.archimedesfluids;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMixinFlowingFluid {
    void changeFluid(World world, BlockPos pos, int amount);
}
