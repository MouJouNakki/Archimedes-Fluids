package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.FluidPool;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(PistonBlock.class)
public abstract class MixinPistonBlock {
    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        PistonHandler pistonHandler = new PistonHandler(world, pos, dir, retract);
        if(!pistonHandler.calculatePush())
            return;
        List<BlockPos> toDestroy = Objects.requireNonNull(pistonHandler).getBrokenBlocks();
        for (BlockPos pos1 : toDestroy) {
            FluidState fluidState = world.getFluidState(pos1);
            if (fluidState.isEmpty()) {
                continue;
            }
            FluidPool fluidPool = new FluidPool(world, pos1, fluidState.getFluid());
            for (BlockPos pos2 : toDestroy) {
                fluidPool.setBanned(pos2);
            }
            if(!fluidPool.checkForSpace(fluidState.getLevel())) {
                callbackInfoReturnable.setReturnValue(false);
                return;
            }
        }
        for (BlockPos pos1 : toDestroy) {
            FluidState fluidState = world.getFluidState(pos1);
            if (fluidState.isEmpty()) {
                continue;
            }
            FluidPool fluidPool = new FluidPool(world, pos1, fluidState.getFluid());
            for (BlockPos pos2 : toDestroy) {
                fluidPool.setBanned(pos2);
            }
            if(!fluidPool.addFluid(fluidState.getLevel())) {
                callbackInfoReturnable.setReturnValue(false);
                return;
            }
        }
    }
}
