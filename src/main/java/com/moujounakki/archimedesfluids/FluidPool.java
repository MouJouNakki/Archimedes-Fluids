package com.moujounakki.archimedesfluids;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;

import java.util.HashSet;
import java.util.LinkedList;

public class FluidPool {
    private final World world;
    private final Fluid fluid;
    private final HashSet<BlockPos> explored = new HashSet<>();
    private final LinkedList<BlockPos> unexplored = new LinkedList<>();
    private final HashSet<BlockPos> banned = new HashSet<>();

    public FluidPool(World world, BlockPos pos, Fluid fluid) {
        this.world = world;
        this.unexplored.add(pos);
        this.fluid = fluid;
    }
    public void setBanned(BlockPos pos) {
        banned.add(pos);
    }

    public boolean removeFluid(int amount) {
        HashSet<BlockPos> set = new HashSet<>();
        int foundFluid = 0;
        while(foundFluid < amount) {
            BlockPos pos = explore(false);
            if(pos == null)
                return false;
            if(banned.contains(pos))
                continue;
            FluidState fluidstate = world.getFluidState(pos);
            if(!fluidstate.getFluid().matchesType(fluid))
                continue;
            set.add(pos);
            foundFluid += fluidstate.getLevel();
        }
        for(BlockPos pos : set) {
            FluidState fluidstate = world.getFluidState(pos);
            int transfer = Math.min(fluidstate.getLevel(),amount);
            amount -= transfer;
            ((IMixinFlowingFluid)fluid).changeFluid(world,pos,-transfer);
        }
        return true;
    }
    public boolean addFluid(int amount) {
        HashSet<BlockPos> set = new HashSet<>();
        int foundSpace = 0;
        while(foundSpace < amount) {
            BlockPos pos = explore(true);
            if(pos == null)
                return false;
            if(banned.contains(pos))
                continue;
            if(world.getBlockState(pos).isAir() || canBeFluidlogged(pos)) {
                set.add(pos);
                foundSpace += 8;
                continue;
            }
            FluidState fluidstate = world.getFluidState(pos);
            if(!fluidstate.getFluid().matchesType(fluid))
                continue;
            set.add(pos);
            foundSpace += 8-fluidstate.getLevel();
        }
        for(BlockPos pos : set) {
            FluidState fluidstate = world.getFluidState(pos);
            int transfer = Math.min(8-fluidstate.getLevel(),amount);
            amount -= transfer;
            ((IMixinFlowingFluid)fluid).changeFluid(world,pos,transfer);
        }
        return true;
    }
    public boolean checkForFluid(int amount) {
        int foundFluid = 0;
        while(foundFluid < amount) {
            BlockPos pos = explore(false);
            if(pos == null)
                return false;
            if(banned.contains(pos))
                continue;
            FluidState fluidstate = world.getFluidState(pos);
            if(!fluidstate.getFluid().matchesType(fluid))
                continue;
            foundFluid += fluidstate.getLevel();
        }
        return true;
    }
    public boolean checkForSpace(int amount) {
        int foundSpace = 0;
        while(foundSpace < amount) {
            BlockPos pos = explore(true);
            if(pos == null)
                return false;
            if(banned.contains(pos))
                continue;
            if(world.getBlockState(pos).isAir() || canBeFluidlogged(pos)) {
                foundSpace += 8;
                continue;
            }
            FluidState fluidstate = world.getFluidState(pos);
            if(!fluidstate.getFluid().matchesType(fluid))
                continue;
            foundSpace += 8-fluidstate.getLevel();
        }
        return true;
    }

    private BlockPos explore(boolean lookForAir) {
        if(unexplored.isEmpty())
            return null;
        BlockPos pos = unexplored.removeFirst();
        explored.add(pos);
        for(Direction direction : Direction.values()) {
            BlockPos pos1 = pos.offset(direction);
            if(explored.contains(pos1))
                continue;
            boolean checkForAir = !lookForAir || !(world.getBlockState(pos1).isAir() || canBeFluidlogged(pos1));
            if(checkForAir && !world.getFluidState(pos1).getFluid().matchesType(fluid))
                continue;
            unexplored.add(pos.offset(direction));
        }
        return pos;
    }

    private boolean canBeFluidlogged(BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.contains(ArchimedesFluids.FLUID_LEVEL) && blockState.get(ArchimedesFluids.FLUID_LEVEL) <= 0;
    }
}
