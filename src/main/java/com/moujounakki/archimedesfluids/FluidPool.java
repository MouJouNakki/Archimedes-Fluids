package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class FluidPool {
    private final Level level;
    private final Fluid fluid;
    private final Set<BlockPos> explored = new HashSet<>();
    private final Queue<BlockPos> unexplored = new ArrayDeque<>();
    private final Set<BlockPos> banned = new HashSet<>();

    public FluidPool(Level level, BlockPos pos, Fluid fluid) {
        this.level = level;
        this.unexplored.add(pos);
        this.fluid = fluid;
    }

    public void setBanned(BlockPos pos) {
        banned.add(pos);
    }

    public int removeFluid(int amount, boolean attempt) {
        Set<BlockPos> set = new HashSet<>();
        int foundFluid = 0;

        while (foundFluid < amount) {
            BlockPos pos = explore(false);
            if (pos == null) {
                if (attempt)
                    break;
                else
                    return -1;
            }

            if (banned.contains(pos)) {
                continue;
            }

            FluidState fluidstate = level.getFluidState(pos);
            if (!fluidstate.getType().isSame(fluid)) {
                continue;
            }

            set.add(pos);
            foundFluid += fluidstate.getAmount();
        }
        int totalTransfer = 0;
        for (BlockPos pos : set) {
            FluidState fluidstate = level.getFluidState(pos);
            int transfer = Math.min(fluidstate.getAmount(), amount);
            amount -= transfer;
            totalTransfer += transfer;
            ((IMixinFlowingFluid) fluid).changeFluid(level, pos, -transfer);
        }

        return totalTransfer;
    }
    public boolean removeFluid(int amount) {
        return removeFluid(amount, false) != -1;
    }
    public int addFluid(int amount, boolean attempt) {
        Set<BlockPos> set = new HashSet<>();
        int foundSpace = 0;

        while (foundSpace < amount) {
            BlockPos pos = explore(true);
            if (pos == null) {
                if (attempt)
                    break;
                else
                    return -1;
            }

            if (banned.contains(pos)) {
                continue;
            }

            if (level.getBlockState(pos).isAir() || canBeFluidlogged(pos)) {
                set.add(pos);
                foundSpace += 8;
                continue;
            }

            FluidState fluidstate = level.getFluidState(pos);
            if (!fluidstate.getType().isSame(fluid)) {
                continue;
            }

            set.add(pos);
            foundSpace += 8 - fluidstate.getAmount();
        }
        int totalTransfer = 0;
        for (BlockPos pos : set) {
            FluidState fluidstate = level.getFluidState(pos);
            int transfer = Math.min(8 - fluidstate.getAmount(), amount);
            amount -= transfer;
            totalTransfer += transfer;
            ((IMixinFlowingFluid) fluid).changeFluid(level, pos, transfer);
        }

        return totalTransfer;
    }
    public boolean addFluid(int amount) {
        return addFluid(amount, false) != -1;
    }

    public boolean checkForFluid(int amount) {
        int foundFluid = 0;

        while (foundFluid < amount) {
            BlockPos pos = explore(false);
            if (pos == null) {
                return false;
            }

            if (banned.contains(pos)) {
                continue;
            }

            FluidState fluidstate = level.getFluidState(pos);
            if (!fluidstate.getType().isSame(fluid)) {
                continue;
            }

            foundFluid += fluidstate.getAmount();
        }

        return true;
    }

    public boolean checkForSpace(int amount) {
        int foundSpace = 0;

        while (foundSpace < amount) {
            BlockPos pos = explore(true);
            if (pos == null) {
                return false;
            }

            if (banned.contains(pos)) {
                continue;
            }

            if (level.getBlockState(pos).isAir() || canBeFluidlogged(pos)) {
                foundSpace += 8;
                continue;
            }

            FluidState fluidstate = level.getFluidState(pos);
            if (!fluidstate.getType().isSame(fluid)) {
                continue;
            }

            foundSpace += 8 - fluidstate.getAmount();
        }

        return true;
    }

    private BlockPos explore(boolean lookForAir) {
        BlockPos pos;
        while ((pos = unexplored.poll()) != null) {
            explored.add(pos);

            for (Direction direction : Direction.values()) {
                BlockPos pos1 = pos.relative(direction);

                if (explored.contains(pos1)) {
                    continue;
                }

                boolean checkForAir = !lookForAir || !(level.getBlockState(pos1).isAir() || canBeFluidlogged(pos1));

                if (checkForAir && !level.getFluidState(pos1).getType().isSame(fluid)) {
                    continue;
                }

                unexplored.add(pos1);
            }

            return pos;
        }

        return null;
    }

    private boolean canBeFluidlogged(BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        return blockState.hasProperty(ArchimedesFluids.FLUID_LEVEL) && blockState.getValue(ArchimedesFluids.FLUID_LEVEL) <= 0;
    }
}
