package com.moujounakki.fluidmotionoverhaul.mixins;

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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FlowingFluid.class)
@SuppressWarnings("unused")
public abstract class MixinFlowingFluid extends Fluid {
    public void tick(Level level, BlockPos pos, FluidState state) {
        BlockState blockstate = level.getBlockState(pos.below());
        FluidState fluidstate = blockstate.getFluidState();
        FluidSpreadType spreadType = this.getFluidSpreadType(blockstate);
        if(spreadType == FluidSpreadType.REPLACE) {
            if (!blockstate.isAir()) {
                this.beforeDestroyingBlock(level, pos.below(), blockstate);
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(pos.below(),this.getFlowing(this.getAmount(state),this.isFallingAt(level, pos)).createLegacyBlock(),3);
        }
        else if(spreadType == FluidSpreadType.ADD && fluidstate.getAmount() < 8) {
            int otherAmount = fluidstate.getAmount();
            int transfer = Math.min(this.getAmount(state),8-otherAmount);
            if(transfer >= this.getAmount(state))
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            else
                level.setBlock(pos, this.getFlowing(this.getAmount(state)-transfer,this.isFallingAt(level,pos)).createLegacyBlock(), 3);
            level.setBlock(pos.below(), this.getFlowing(otherAmount+transfer,this.isFallingAt(level, pos.below())).createLegacyBlock(), 3);
        }
        else if(this.getAmount(state) > 1) {
            for(Direction direction : Direction.Plane.HORIZONTAL.shuffledCopy(level.getRandom())) {
                BlockPos pos1 = pos.relative(direction);
                BlockState blockstate1 = level.getBlockState(pos1);
                FluidState fluidstate1 = blockstate1.getFluidState();
                FluidSpreadType spreadType1 = this.getFluidSpreadType(blockstate1);
                if(spreadType1 == FluidSpreadType.REPLACE) {
                    if(!blockstate1.isAir()) {
                        this.beforeDestroyingBlock(level, pos1, blockstate1);
                    }
                    level.setBlock(pos1, this.getFlowing(1,this.isFallingAt(level, pos1)).createLegacyBlock(), 3);
                    level.setBlock(pos, this.getFlowing(this.getAmount(state)-1,this.isFallingAt(level, pos)).createLegacyBlock(), 3);
                    break;
                }
                else if(spreadType1 == FluidSpreadType.ADD) {
                    int otherAmount = fluidstate1.getAmount();
                    if(this.getAmount(state) > otherAmount) {
                        level.setBlock(pos1, this.getFlowing(otherAmount+1,this.isFallingAt(level, pos1)).createLegacyBlock(), 3);
                        level.setBlock(pos, this.getFlowing(this.getAmount(state)-1,this.isFallingAt(level, pos)).createLegacyBlock(), 3);
                        break;
                    }
                }
            }
        }
        else if(this.getAmount(state) == 1 && Math.random() < 0.3) {
            for(Direction direction : Direction.Plane.HORIZONTAL.shuffledCopy(level.getRandom())) {
                BlockPos pos1 = pos.relative(direction);
                BlockState blockstate1 = level.getBlockState(pos1);
                FluidSpreadType spreadType1 = this.getFluidSpreadType(blockstate1);
                if(spreadType1 == FluidSpreadType.REPLACE) {
                    if(!blockstate1.isAir()) {
                        this.beforeDestroyingBlock(level, pos1, blockstate1);
                    }
                    level.setBlock(pos1, this.getFlowing(1,this.isFallingAt(level, pos1)).createLegacyBlock(), 3);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
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
        return (!fluidstate2.isEmpty() && fluidstate2.is(this) && this.canPassThroughWall(Direction.UP, reader, pos, blockstate, blockpos1, blockstate2));
    }
    @SuppressWarnings("SameReturnValue")
    @Shadow
    private boolean canPassThroughWall(Direction p_76062_, BlockGetter p_76063_, BlockPos p_76064_, BlockState p_76065_, BlockPos p_76066_, BlockState p_76067_) {
        return false;
    }
    @Shadow
    public abstract FluidState getFlowing(int p_75954_, boolean p_75955_);
    @Shadow
    protected abstract void beforeDestroyingBlock(LevelAccessor p_76002_, BlockPos p_76003_, BlockState p_76004_);

    @Overwrite
    protected static int getLegacyLevel(FluidState p_76093_) {
        return 8 - Math.min(p_76093_.getAmount(), 8);
    }
    private enum FluidSpreadType {
        REPLACE,
        ADD,
        BLOCKED
    }
    private FluidSpreadType getFluidSpreadType(BlockState blockstate) {
        FluidState fluidstate = blockstate.getFluidState();
        if(blockstate.canBeReplaced(this) && fluidstate.isEmpty()) {
            return FluidSpreadType.REPLACE;
        }
        else if(fluidstate.is(this)) {
            return FluidSpreadType.ADD;
        }
        return FluidSpreadType.BLOCKED;
    }
}
