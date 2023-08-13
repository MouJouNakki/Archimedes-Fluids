package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.ArchimedesFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBase {
    @Inject(method = "neighborChanged", at = @At("HEAD"))
    public void onNeighborChanged(Level level, BlockPos pos, Block block, BlockPos pos1, boolean bool, CallbackInfo callbackInfo) {
        if(level.isClientSide)
            return;
        BlockState state = this.asState();
        if(!state.hasProperty(ArchimedesFluids.FLUID_LEVEL))
            return;
        if(state.getValue(ArchimedesFluids.FLUID_LEVEL) <= 0)
            return;
        Fluid fluid = state.getValue(ArchimedesFluids.FLUIDLOGGED).fluid;
        level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
    }
    @Shadow
    protected abstract BlockState asState();
}
