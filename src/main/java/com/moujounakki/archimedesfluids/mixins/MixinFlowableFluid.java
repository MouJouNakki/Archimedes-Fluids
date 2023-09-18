package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FlowableFluid.class)
@SuppressWarnings("unused")
public abstract class MixinFlowableFluid extends Fluid implements IMixinFlowingFluid {
    public void onScheduledTick(World world, BlockPos pos, FluidState state) {
        BlockState blockstate = world.getBlockState(pos.down());
        FluidState fluidstate = blockstate.getFluidState();
        FluidSpreadType spreadType = this.getFluidSpreadType(blockstate);
        int amount = this.getLevel(state);
        if(spreadType == FluidSpreadType.REPLACE) {
            if (!blockstate.isAir()) {
                this.beforeBreakingBlock(world, pos.down(), blockstate);
            }
            this.transferFluid(world, pos, pos.down(), amount);
        }
        else if(spreadType == FluidSpreadType.ADD && fluidstate.getLevel() < 8) {
            int otherAmount = fluidstate.getLevel();
            int transfer = Math.min(amount,8-otherAmount);
            this.transferFluid(world, pos, pos.down(), transfer);
        }
        else if(amount > 1) {
            for(Direction direction : Direction.Type.HORIZONTAL.getShuffled(world.getRandom())) {
                BlockPos pos1 = pos.offset(direction);
                BlockState blockstate1 = world.getBlockState(pos1);
                FluidState fluidstate1 = blockstate1.getFluidState();
                FluidSpreadType spreadType1 = this.getFluidSpreadType(blockstate1);
                if(spreadType1 == FluidSpreadType.REPLACE) {
                    if(!blockstate1.isAir()) {
                        this.beforeBreakingBlock(world, pos1, blockstate1);
                    }
                    this.transferFluid(world, pos, pos1);
                    break;
                }
                else if(spreadType1 == FluidSpreadType.ADD) {
                    int otherAmount = fluidstate1.getLevel();
                    if(amount > otherAmount) {
                        this.transferFluid(world, pos, pos1);
                        break;
                    }
                }
            }
        }
        else if(amount == 1 && Math.random() < 0.3) {
            for(Direction direction : Direction.Type.HORIZONTAL.getShuffled(world.getRandom())) {
                BlockPos pos1 = pos.offset(direction);
                BlockState blockstate1 = world.getBlockState(pos1);
                FluidSpreadType spreadType1 = this.getFluidSpreadType(blockstate1);
                if(spreadType1 == FluidSpreadType.REPLACE) {
                    if(!blockstate1.isAir()) {
                        this.beforeBreakingBlock(world, pos1, blockstate1);
                    }
                    this.transferFluid(world, pos, pos1);
                    break;
                }
            }
        }
    }
    private boolean isFallingAt(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockPos blockPos2;
        BlockState blockState3;
        FluidState fluidState3;
        return !(fluidState3 = (blockState3 = world.getBlockState(blockPos2 = pos.up())).getFluidState()).isEmpty() && fluidState3.getFluid().matchesType(this) && this.receivesFlow(Direction.UP, world, pos, state, blockPos2, blockState3);
    }
    @Shadow
    protected abstract boolean isFlowBlocked(BlockView world, BlockPos pos, Direction direction);
    @Shadow
    private boolean receivesFlow(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        return false;
    }
    private void transferFluid(WorldAccess world, BlockPos from, BlockPos to) {
        this.transferFluid(world, from, to, world.getFluidState(from).getLevel(), world.getFluidState(to).getLevel());
    }
    private void transferFluid(WorldAccess world, BlockPos from, BlockPos to, int transfer) {
        this.transferFluid(world, from, to, world.getFluidState(from).getLevel(), world.getFluidState(to).getLevel(), transfer);
    }
    private void transferFluid(WorldAccess world, BlockPos from, BlockPos to, int fromAmount, int toAmount) {
        this.transferFluid(world, from, to, fromAmount, toAmount, 1);
    }
    private void transferFluid(WorldAccess world, BlockPos from, BlockPos to, int fromAmount, int toAmount, int transfer) {
        this.setFlowing(world, to, toAmount+transfer);
        this.setFlowing(world, from, fromAmount-transfer);
    }
    private void setFlowing(WorldAccess world, BlockPos pos, int amount) {
        BlockState blockState = world.getBlockState(pos);
        if(blockState.contains(ArchimedesFluids.FLUID_LEVEL)) {
            Fluidlogging fluidlogging = FluidloggingProperty.getFluidLogging((FlowableFluid)getFlowing(1,false).getFluid());
            if(fluidlogging != null) {
                world.setBlockState(pos, blockState
//                    .setValue(ArchimedesFluids.FLUIDLOGGED, new Fluidlogging(this))
                        .with(ArchimedesFluids.FLUIDLOGGED, fluidlogging)
                        .with(ArchimedesFluids.FLUID_LEVEL, amount)
                        .with(Properties.WATERLOGGED, false), 3);
                world.scheduleFluidTick(pos, this, this.getTickRate(world));
                return;
            }
        }
        if(amount < 1) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            return;
        }
        world.setBlockState(pos, this.getFlowing(amount, this.isFallingAt((World) world, pos)).getBlockState(), 3);
    }

    @Override
    public void changeFluid(World world, BlockPos pos, int amount) {
        int current = world.getFluidState(pos).getLevel();
        if(current+amount > 8)
            throw new IllegalArgumentException("Cannot add over 8 fluid(current %d, trying to add: %d)".formatted(current, amount));
        this.setFlowing(world, pos, current+amount);
    }

    @Shadow
    public abstract FluidState getFlowing(int p_75954_, boolean p_75955_);
    @Shadow
    protected abstract void beforeBreakingBlock(WorldAccess p_76002_, BlockPos p_76003_, BlockState p_76004_);

    @Overwrite
    public static int getBlockStateLevel(FluidState state) {
        return 8-Math.min(state.getLevel(), 8);
    }
    private FluidSpreadType getFluidSpreadType(BlockState blockstate) {
        FluidState fluidstate = blockstate.getFluidState();
        if(blockstate.canBucketPlace(this) && fluidstate.isEmpty()) {
            return FluidSpreadType.REPLACE;
        }
        else if(fluidstate.getFluid().matchesType(this)) {
            return FluidSpreadType.ADD;
        }
        else if(fluidstate.isEmpty() && blockstate.contains(ArchimedesFluids.FLUID_LEVEL)) {
            return FluidSpreadType.ADD;
        }
        return FluidSpreadType.BLOCKED;
    }
    @Overwrite
    public float getHeight(FluidState state, BlockView blockView, BlockPos pos) {
        if(isFluidAboveEqual(state, blockView, pos))
            return 1.0F;
        int found = 0;
        int amount = state.getLevel();
        for(Direction direction : Direction.Type.HORIZONTAL) {
            FluidState state1 = blockView.getFluidState(pos.offset(direction));
            if(!state1.getFluid().matchesType(this)) {
                continue;
            }
            if(state1.getLevel() != amount+1) {
                continue;
            }
            found++;
            if(found == 2)
                break;
        }
        if(found == 2)
            return (amount+1)/9.0F;
        return state.getHeight();
    }
    @Overwrite
    public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
        double d = 0.0;
        double e = 0.0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            mutable.set((Vec3i)pos, direction);
            FluidState fluidState = world.getFluidState(mutable);
            if (!this.isEmptyOrThis(fluidState)) continue;
            float f = fluidState.getHeight();
            float g = 0.0f;
            if (f == 0.0f) {
                Vec3i blockPos;
                FluidState fluidState2;
                if (!world.getBlockState(mutable).blocksMovement() && this.isEmptyOrThis(fluidState2 = world.getFluidState((BlockPos)(blockPos = mutable.down()))) && (f = fluidState2.getHeight()) > 0.0f) {
                    g = state.getHeight() - (f - 0.8888889f);
                }
            } else if (f > 0.0f) {
                g = state.getHeight() - f;
            }
            if (g == 0.0f) continue;
            d += (double)((float)direction.getOffsetX() * g);
            e += (double)((float)direction.getOffsetZ() * g);
        }
        Vec3d vec3d = new Vec3d(d, 0.0, e);
        if (state.get(FALLING).booleanValue()) {
            for (Direction direction2 : Direction.Type.HORIZONTAL) {
                mutable.set((Vec3i)pos, direction2);
                if (!this.isFlowBlocked(world, mutable, direction2) && !this.isFlowBlocked(world, (BlockPos)mutable.up(), direction2)) continue;
                vec3d = vec3d.normalize().add(0.0, -6.0, 0.0);
                break;
            }
        }
        return vec3d.normalize();
    }
    @Shadow
    private boolean isEmptyOrThis(FluidState state) {
        return false;
    }
    @Shadow
    private static boolean isFluidAboveEqual(FluidState p_76089_, BlockView p_76090_, BlockPos p_76091_) {
        return false;
    }
    @Shadow
    public static final BooleanProperty FALLING = Properties.FALLING;
}
