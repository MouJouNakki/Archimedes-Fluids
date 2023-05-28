package com.moujounakki.fluidmotionoverhaul.mixins;

import com.moujounakki.fluidmotionoverhaul.IMixinFlowingFluid;
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
        if(blockstate.canBeReplaced(state.getType()) && blockstate.getFluidState().getType().isSame(Fluids.EMPTY)) {
            if (!blockstate.isAir()) {
                this.beforeDestroyingBlock(level, pos.below(), blockstate);
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(pos.below(),this.getFlowing(this.getAmount(state),this.isFallingAt(level, pos)).createLegacyBlock(),3);
        }
        else if(blockstate.getFluidState().getType().isSame(this) && blockstate.getFluidState().getType().getAmount(blockstate.getFluidState()) < 8) {
            int otherAmount = blockstate.getFluidState().getType().getAmount(blockstate.getFluidState());
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
                if(blockstate1.canBeReplaced(state.getType()) && blockstate1.getFluidState().getType().isSame(Fluids.EMPTY)) {
                    if(!blockstate1.isAir()) {
                        this.beforeDestroyingBlock(level, pos1, blockstate1);
                    }
                    level.setBlock(pos1, this.getFlowing(1,this.isFallingAt(level, pos1)).createLegacyBlock(), 3);
                    level.setBlock(pos, this.getFlowing(this.getAmount(state)-1,this.isFallingAt(level, pos)).createLegacyBlock(), 3);
                    break;
                }
                else if(blockstate1.getFluidState().getType().isSame(this)) {
                    int otherAmount = blockstate1.getFluidState().getType().getAmount(blockstate1.getFluidState());
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
                if(blockstate1.canBeReplaced(state.getType()) && blockstate1.getFluidState().getType().isSame(Fluids.EMPTY)) {
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
        return (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.canPassThroughWall(Direction.UP, reader, pos, blockstate, blockpos1, blockstate2));
    }
    @Shadow
    private boolean canPassThroughWall(Direction p_76062_, BlockGetter p_76063_, BlockPos p_76064_, BlockState p_76065_, BlockPos p_76066_, BlockState p_76067_) {
        return false;
    }
    @Shadow
    public FluidState getFlowing(int p_75954_, boolean p_75955_) {
        return null;
    }
    @Shadow
    protected abstract void beforeDestroyingBlock(LevelAccessor p_76002_, BlockPos p_76003_, BlockState p_76004_);

    @Overwrite
    protected static int getLegacyLevel(FluidState p_76093_) {
        return 8 - Math.min(p_76093_.getAmount(), 8);
    }
    public boolean checkForFluidInWay(LevelAccessor level, BlockPos pos, FluidState state) {
        return findSpaceForFluid(level, pos, state) == null;
    }
    public void moveFluidInWay(LevelAccessor level, BlockPos pos, FluidState state) {
        if(level.getBlockState(pos).getFluidState().is(Fluids.EMPTY))
            return;
        BlockPos pos1 = findSpaceForFluid(level, pos, state);
        assert pos1 != null;
        BlockState blockstate1 = level.getBlockState(pos1);
        FluidState fluidstate1 = blockstate1.getFluidState();
        if(blockstate1.canBeReplaced(this) && fluidstate1.isEmpty()) {
            if(!blockstate1.isAir()) {
                this.beforeDestroyingBlock(level, pos1, blockstate1);
            }
            level.setBlock(pos1, this.getFlowing(this.getAmount(state),this.isFallingAt(level, pos1)).createLegacyBlock(), 3);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        else if(fluidstate1.is(this)) {
            int otherAmount = fluidstate1.getAmount();
            level.setBlock(pos1, this.getFlowing(otherAmount+state.getAmount(),this.isFallingAt(level, pos1)).createLegacyBlock(), 3);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
    private BlockPos findSpaceForFluid(LevelAccessor level, BlockPos pos, FluidState state) {
        for(Direction direction : Direction.allShuffled(level.getRandom())) {
            BlockPos pos1 = pos.relative(direction);
            BlockState blockstate1 = level.getBlockState(pos1);
            FluidState fluidstate1 = blockstate1.getFluidState();
            if(blockstate1.canBeReplaced(this) && fluidstate1.getType().isSame(Fluids.EMPTY)) {
                return pos1;
            }
            else if(fluidstate1.getType().isSame(this)) {
                int otherAmount = fluidstate1.getType().getAmount(fluidstate1);
                if(this.getAmount(state)+otherAmount <= 8) {
                    return pos1;
                }
            }
        }
        return null;
    }
}
