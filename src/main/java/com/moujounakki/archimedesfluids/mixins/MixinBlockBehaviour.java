package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.ArchimedesFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class MixinBlockBehaviour {
    @Overwrite
    public FluidState getFluidState(BlockState state) {
        if(!state.hasProperty(ArchimedesFluids.FLUID_LEVEL))
            return Fluids.EMPTY.defaultFluidState();
        if(state.getValue(ArchimedesFluids.FLUID_LEVEL) <= 0)
            return Fluids.EMPTY.defaultFluidState();
        return ((FlowingFluid)(state.getValue(ArchimedesFluids.FLUIDLOGGED).fluid)).getFlowing(state.getValue(ArchimedesFluids.FLUID_LEVEL),false);
    }
    @Inject(method = "updateShape", at = @At("HEAD"))
    private void onUpdateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor levelAccessor, BlockPos pos, BlockPos pos1, CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if(!state.hasProperty(ArchimedesFluids.FLUID_LEVEL))
            return;
        if(state.getValue(ArchimedesFluids.FLUID_LEVEL) <= 0)
            return;
        Fluid fluid = state.getValue(ArchimedesFluids.FLUIDLOGGED).fluid;
        levelAccessor.scheduleTick(pos, fluid, fluid.getTickDelay(levelAccessor));
    }
}
