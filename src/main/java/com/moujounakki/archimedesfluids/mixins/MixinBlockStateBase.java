package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.ArchimedesFluids;
import com.moujounakki.archimedesfluids.FluidloggingData;
import com.moujounakki.archimedesfluids.IMixinBlockStateBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBase implements IMixinBlockStateBase {
    private FluidState fluidloggingState;
    @Override
    public FluidState getFluidloggingState() {
        return fluidloggingState;
    }

    @Override
    public void setFluidloggingState(FluidState state) {
        this.fluidloggingState = state;
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"))
    public void onNeighborChanged(Level level, BlockPos pos, Block block, BlockPos pos1, boolean bool, CallbackInfo callbackInfo) {
        if(level.isClientSide)
            return;
        BlockState state = this.asState();
        if(!state.hasProperty(BlockStateProperties.WATERLOGGED))
            return;
        FluidState fluidstate = FluidloggingData.getFluid(level, pos);
        if(fluidstate.getAmount() <= 0)
            return;
        Fluid fluid = fluidstate.getType();
        level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
    }
    @Shadow
    protected abstract BlockState asState();
}
