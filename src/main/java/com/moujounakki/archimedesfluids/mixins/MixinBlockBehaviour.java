package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.ArchimedesFluids;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockBehaviour.class)
@SuppressWarnings("unused")
public abstract class MixinBlockBehaviour {
    @Overwrite
    public FluidState getFluidState(BlockState state) {
        if(!state.hasProperty(ArchimedesFluids.WATER_LEVEL))
            return Fluids.EMPTY.defaultFluidState();
        if(state.getValue(ArchimedesFluids.WATER_LEVEL) <= 0)
            return Fluids.EMPTY.defaultFluidState();
        return Fluids.WATER.getFlowing(state.getValue(ArchimedesFluids.WATER_LEVEL),false);
    }
}
