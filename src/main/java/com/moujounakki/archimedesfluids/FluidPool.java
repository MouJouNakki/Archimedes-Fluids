package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.HashSet;
import java.util.LinkedList;

public class FluidPool {
    private final Level level;
    private final Fluid fluid;
    private final HashSet<BlockPos> explored = new HashSet<>();
    private final LinkedList<BlockPos> unexplored = new LinkedList<>();
    private final HashSet<BlockPos> banned = new HashSet<>();

    public FluidPool(Level level, BlockPos pos, Fluid fluid) {
        this.level = level;
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
            FluidState fluidstate = level.getFluidState(pos);
            if(!fluidstate.getType().isSame(fluid))
                continue;
            set.add(pos);
            foundFluid += fluidstate.getAmount();
        }
        for(BlockPos pos : set) {
            FluidState fluidstate = level.getFluidState(pos);
            int transfer = Math.min(fluidstate.getAmount(),amount);
            amount -= transfer;
            ((IMixinFlowingFluid)fluid).changeFluid(level,pos,-transfer);
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
            if(level.getBlockState(pos).isAir() || canBeFluidlogged(pos)) {
                set.add(pos);
                foundSpace += 8;
                continue;
            }
            FluidState fluidstate = level.getFluidState(pos);
            if(!fluidstate.getType().isSame(fluid))
                continue;
            set.add(pos);
            foundSpace += 8-fluidstate.getAmount();
        }
        for(BlockPos pos : set) {
            FluidState fluidstate = level.getFluidState(pos);
            int transfer = Math.min(8-fluidstate.getAmount(),amount);
            amount -= transfer;
            ((IMixinFlowingFluid)fluid).changeFluid(level,pos,transfer);
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
            FluidState fluidstate = level.getFluidState(pos);
            if(!fluidstate.getType().isSame(fluid))
                continue;
            foundFluid += fluidstate.getAmount();
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
            if(level.getBlockState(pos).isAir() || canBeFluidlogged(pos)) {
                foundSpace += 8;
                continue;
            }
            FluidState fluidstate = level.getFluidState(pos);
            if(!fluidstate.getType().isSame(fluid))
                continue;
            foundSpace += 8-fluidstate.getAmount();
        }
        return true;
    }

    private BlockPos explore(boolean lookForAir) {
        if(unexplored.isEmpty())
            return null;
        BlockPos pos = unexplored.removeFirst();
        explored.add(pos);
        for(Direction direction : Direction.values()) {
            BlockPos pos1 = pos.relative(direction);
            if(explored.contains(pos1))
                continue;
            boolean checkForAir = !lookForAir || !(level.getBlockState(pos1).isAir() || canBeFluidlogged(pos1));
            if(checkForAir && !level.getFluidState(pos1).getType().isSame(fluid))
                continue;
            unexplored.add(pos.relative(direction));
        }
        return pos;
    }

    private boolean canBeFluidlogged(BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        return blockState.hasProperty(BlockStateProperties.WATERLOGGED) && FluidloggingData.getFluid(level, pos).getAmount() <= 0;
    }
}
