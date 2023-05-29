package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.FluidSpreadType;
import com.moujounakki.archimedesfluids.IMixinFlowingFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FlowingFluid.class)
@SuppressWarnings("unused")
public abstract class MixinFlowingFluid extends Fluid implements IMixinFlowingFluid {
    public void tick(Level level, BlockPos pos, FluidState state) {
        BlockState blockstate = level.getBlockState(pos.below());
        FluidState fluidstate = blockstate.getFluidState();
        FluidSpreadType spreadType = this.getFluidSpreadType(blockstate);
        int amount = this.getAmount(state);
        if(spreadType == FluidSpreadType.REPLACE) {
            if (!blockstate.isAir()) {
                this.beforeDestroyingBlock(level, pos.below(), blockstate);
            }
            this.transferFluid(level, pos, pos.below(), amount);
        }
        else if(spreadType == FluidSpreadType.ADD && fluidstate.getAmount() < 8) {
            int otherAmount = fluidstate.getAmount();
            int transfer = Math.min(amount,8-otherAmount);
            this.transferFluid(level, pos, pos.below(), transfer);
        }
        else if(amount > 1) {
            for(Direction direction : Direction.Plane.HORIZONTAL.shuffledCopy(level.getRandom())) {
                BlockPos pos1 = pos.relative(direction);
                BlockState blockstate1 = level.getBlockState(pos1);
                FluidState fluidstate1 = blockstate1.getFluidState();
                FluidSpreadType spreadType1 = this.getFluidSpreadType(blockstate1);
                if(spreadType1 == FluidSpreadType.REPLACE) {
                    if(!blockstate1.isAir()) {
                        this.beforeDestroyingBlock(level, pos1, blockstate1);
                    }
                    this.transferFluid(level, pos, pos1);
                    break;
                }
                else if(spreadType1 == FluidSpreadType.ADD) {
                    int otherAmount = fluidstate1.getAmount();
                    if(amount > otherAmount) {
                        this.transferFluid(level, pos, pos1);
                        break;
                    }
                }
            }
        }
        else if(amount == 1 && Math.random() < 0.3) {
            for(Direction direction : Direction.Plane.HORIZONTAL.shuffledCopy(level.getRandom())) {
                BlockPos pos1 = pos.relative(direction);
                BlockState blockstate1 = level.getBlockState(pos1);
                FluidSpreadType spreadType1 = this.getFluidSpreadType(blockstate1);
                if(spreadType1 == FluidSpreadType.REPLACE) {
                    if(!blockstate1.isAir()) {
                        this.beforeDestroyingBlock(level, pos1, blockstate1);
                    }
                    this.transferFluid(level, pos, pos1);
                    break;
                }
            }
        }
    }
    private boolean isFallingAt(LevelReader reader, BlockPos pos) {
        BlockState blockstate = reader.getBlockState(pos);
        BlockPos blockpos1 = pos.above();
        BlockState blockstate2 = reader.getBlockState(blockpos1);
        FluidState fluidstate2 = blockstate2.getFluidState();
        return (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.canPassThroughWall(Direction.UP, reader, pos, blockstate, blockpos1, blockstate2));
    }
    @SuppressWarnings("SameReturnValue")
    @Shadow
    private boolean canPassThroughWall(Direction p_76062_, BlockGetter p_76063_, BlockPos p_76064_, BlockState p_76065_, BlockPos p_76066_, BlockState p_76067_) {
        return false;
    }
    private void transferFluid(LevelAccessor level, BlockPos from, BlockPos to) {
        this.transferFluid(level, from, to, level.getFluidState(from).getAmount(), level.getFluidState(to).getAmount());
    }
    private void transferFluid(LevelAccessor level, BlockPos from, BlockPos to, int transfer) {
        this.transferFluid(level, from, to, level.getFluidState(from).getAmount(), level.getFluidState(to).getAmount(), transfer);
    }
    private void transferFluid(LevelAccessor level, BlockPos from, BlockPos to, int fromAmount, int toAmount) {
        this.transferFluid(level, from, to, fromAmount, toAmount, 1);
    }
    private void transferFluid(LevelAccessor level, BlockPos from, BlockPos to, int fromAmount, int toAmount, int transfer) {
        this.setFlowing(level, to, toAmount+transfer);
        this.setFlowing(level, from, fromAmount-transfer);
    }
    private void setFlowing(LevelAccessor level, BlockPos pos, int amount) {
        if(amount < 1) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        level.setBlock(pos, this.getFlowing(amount, this.isFallingAt(level, pos)).createLegacyBlock(), 3);
    }
    @Shadow
    public abstract FluidState getFlowing(int p_75954_, boolean p_75955_);
    @Shadow
    protected abstract void beforeDestroyingBlock(LevelAccessor p_76002_, BlockPos p_76003_, BlockState p_76004_);

    @Overwrite
    protected static int getLegacyLevel(FluidState p_76093_) {
        return 8 - Math.min(p_76093_.getAmount(), 8);
    }
    private FluidSpreadType getFluidSpreadType(BlockState blockstate) {
        FluidState fluidstate = blockstate.getFluidState();
        if(blockstate.canBeReplaced(this) && fluidstate.isEmpty()) {
            return FluidSpreadType.REPLACE;
        }
        else if(fluidstate.getType().isSame(this)) {
            return FluidSpreadType.ADD;
        }
        return FluidSpreadType.BLOCKED;
    }
    public boolean checkForFluidInWay(LevelAccessor level, BlockPos pos, FluidState state) {
        return findSpaceForFluid(level, pos, state) == null;
    }
    public void moveFluidInWay(LevelAccessor level, BlockPos pos, FluidState state) {
        BlockPos pos1 = findSpaceForFluid(level, pos, state);
        assert pos1 != null;
        BlockState blockstate1 = level.getBlockState(pos1);
        FluidState fluidstate1 = blockstate1.getFluidState();
        FluidSpreadType spreadType = this.getFluidSpreadType(blockstate1);
        if(spreadType == FluidSpreadType.REPLACE) {
            if(!blockstate1.isAir()) {
                this.beforeDestroyingBlock(level, pos1, blockstate1);
            }
            this.transferFluid(level, pos, pos1, this.getAmount(state));
        }
        else if(spreadType == FluidSpreadType.ADD) {
            int otherAmount = fluidstate1.getAmount();
            this.transferFluid(level, pos, pos1, this.getAmount(state));
        }
    }
    private BlockPos findSpaceForFluid(LevelAccessor level, BlockPos pos, FluidState state) {
        for(Direction direction : Direction.allShuffled(level.getRandom())) {
            BlockPos pos1 = pos.relative(direction);
            BlockState blockstate1 = level.getBlockState(pos1);
            FluidState fluidstate1 = blockstate1.getFluidState();
            FluidSpreadType spreadType = this.getFluidSpreadType(blockstate1);
            if(spreadType == FluidSpreadType.REPLACE) {
                return pos1;
            }
            else if(spreadType == FluidSpreadType.ADD) {
                int otherAmount = fluidstate1.getType().getAmount(fluidstate1);
                if(this.getAmount(state)+otherAmount <= 8) {
                    return pos1;
                }
            }
        }
        return null;
    }
}
