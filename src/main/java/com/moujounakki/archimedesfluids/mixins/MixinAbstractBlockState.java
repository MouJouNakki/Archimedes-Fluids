package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.ArchimedesFluids;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {
    @Overwrite
    public FluidState getFluidState() {
        BlockState state = asBlockState();
        if(!state.contains(ArchimedesFluids.FLUID_LEVEL))
            return Fluids.EMPTY.getDefaultState();
        if(state.get(ArchimedesFluids.FLUID_LEVEL) <= 0)
            return Fluids.EMPTY.getDefaultState();
        return state.get(ArchimedesFluids.FLUIDLOGGED).fluid.getFlowing(state.get(ArchimedesFluids.FLUID_LEVEL),false);
    }
    @Inject(method = "neighborUpdate", at = @At("HEAD"))
    public void onNeighborUpdate(World world, BlockPos pos, Block block, BlockPos pos1, boolean bool, CallbackInfo callbackInfo) {
        if(world.isClient)
            return;
        BlockState state = this.asBlockState();
        if(!state.contains(ArchimedesFluids.FLUID_LEVEL))
            return;
        if(state.get(ArchimedesFluids.FLUID_LEVEL) <= 0)
            return;
        Fluid fluid = state.get(ArchimedesFluids.FLUIDLOGGED).fluid;
        world.scheduleFluidTick(pos, fluid, fluid.getTickRate(world));
    }
    @Shadow
    protected abstract BlockState asBlockState();
}
