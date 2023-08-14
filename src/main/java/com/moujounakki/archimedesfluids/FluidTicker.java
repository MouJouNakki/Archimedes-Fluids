package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.LinkedList;

public class FluidTicker extends Thread {
    private static final LinkedList<FluidTick> ticks = new LinkedList<>();
    private static final FluidTicker thread = new FluidTicker();
    public static void addTick(Fluid fluid, Level level, BlockPos pos, FluidState state) {
        ticks.addLast(new FluidTick((IMixinFlowingFluid)fluid, level, pos, state));
        if(!thread.isAlive()) {
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void run() {
        while(true) {
            while(ticks.isEmpty())
                FluidTicker.onSpinWait();
            ticks.removeFirst().tick();
        }
    }
}
