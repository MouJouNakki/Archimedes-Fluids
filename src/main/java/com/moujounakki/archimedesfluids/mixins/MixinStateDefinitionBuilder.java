package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.ArchimedesFluids;
import com.moujounakki.archimedesfluids.ArchimedesFluidsCommonConfig;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StateDefinition.Builder.class)
@SuppressWarnings("unused")
public abstract class MixinStateDefinitionBuilder<O, S extends StateHolder<O, S>> {
    @Shadow
    private O owner;
    @Shadow
    public abstract StateDefinition.Builder<O, S> add(Property<?>... p_61105_);
    @Inject(method = "validateProperty", at = @At("HEAD"))
    private <T extends Comparable<T>> void onValidateProperty(Property<T> property, CallbackInfo callbackInfo) {
        if(!ArchimedesFluidsCommonConfig.getFluidlogging())
            return;
        if(owner instanceof WallBlock)
            return;
        if(property == BlockStateProperties.WATERLOGGED)
            add(ArchimedesFluids.FLUIDLOGGED, ArchimedesFluids.FLUID_LEVEL);
    }
}
