package com.moujounakki.archimedesfluids.mixins;

import com.google.common.collect.Maps;
import com.moujounakki.archimedesfluids.ArchimedesFluids;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(StateDefinition.Builder.class)
@SuppressWarnings("unused")
public abstract class MixinStateDefinitionBuilder<O, S extends StateHolder<O, S>> {

    @Inject(method = "add", at = @At("HEAD"))
//    protected void onAddWaterlogged(CallbackInfoReturnable<StateDefinition.Builder<O, S>> callbackInfo, Property<?>... properties) {
//        for(Property<?> property : properties) {
//            if(property == BlockStateProperties.WATERLOGGED)
//                add(ArchimedesFluids.WATER_LEVEL);
//        }
//    }
//    protected void onAddWaterlogged(Property<?> property, CallbackInfoReturnable<StateDefinition.Builder<O, S>> callbackInfo) {
//        if(property == BlockStateProperties.WATERLOGGED)
//            add(ArchimedesFluids.WATER_LEVEL);
//    }
    protected void onAddWaterlogged(Property<?>[] properties, CallbackInfoReturnable<StateDefinition.Builder<O, S>> callbackInfo) {
        for(Property<?> property : properties) {
            if(property == BlockStateProperties.WATERLOGGED)
                add(ArchimedesFluids.WATER_LEVEL);
        }
    }
    @Shadow
    public abstract StateDefinition.Builder<O, S> add(Property<?>... p_61105_);
//    @Shadow
//    private <T extends Comparable<T>> void validateProperty(Property<T> p_61100_) {
//
//    }
//    @Shadow
//    private final Map<String, Property<?>> properties = Maps.newHashMap();
}
