package com.moujounakki.archimedesfluids;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BucketItem;
import net.minecraft.item.BlockItem;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class ArchimedesFluids implements ModInitializer
{
    public static final Logger LOGGER = LoggerFactory.getLogger("archimedesfluids");
    public static final FluidloggingProperty FLUIDLOGGED = new FluidloggingProperty();
    public static final IntProperty FLUID_LEVEL = IntProperty.of("fluid_level",0,8);
    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if(player.isSpectator())
                return ActionResult.PASS;
            Item item = player.getStackInHand(hand).getItem();
            if(item instanceof BlockItem)
                return onPlaceBlock(world, Block.getBlockFromItem(item), hitResult);
            else if(item instanceof BucketItem)
                return onUseBucket(player, hand, world, hitResult);
            return ActionResult.PASS;
        });
    }
    private ActionResult onPlaceBlock(World world, Block block, BlockHitResult hitResult) {
        if(block instanceof FluidFillable && !(block instanceof Waterloggable))
            return ActionResult.PASS;
        FluidState state = world.getFluidState(hitResult.getBlockPos());
        Fluid fluid = state.getFluid();
        if(fluid.matchesType(Fluids.EMPTY))
            return ActionResult.PASS;
        FluidPool fluidPool = new FluidPool(world, hitResult.getBlockPos(), fluid);
        fluidPool.setBanned(hitResult.getBlockPos());
        if(!fluidPool.addFluid(state.getLevel()))
            return ActionResult.FAIL;
        return ActionResult.PASS;
    }
    private ActionResult onUseBucket(PlayerEntity player, Hand hand, World world, BlockHitResult hitResult) {
        ItemStack itemStack = player.getStackInHand(hand);
        BucketItem bucket = (BucketItem)itemStack.getItem();
        Fluid fluid;
        try {
            Field field = BucketItem.class.getDeclaredField("fluid");
            field.setAccessible(true);
            fluid = (Fluid)field.get(bucket);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        BlockPos pos = hitResult.getBlockPos();
        Fluid fluid1 = world.getFluidState(pos).getFluid();
        if(fluid == Fluids.EMPTY) {
            if(fluid1 == Fluids.EMPTY) {
                return ActionResult.FAIL;
            }
            FluidPool fluidPool = new FluidPool(world,pos,fluid1);
            if(fluidPool.removeFluid(8)) {
                ItemStack itemStack1 = new ItemStack(fluid1.getBucketItem());
                if(itemStack.getCount() == 1) {
                    player.setStackInHand(hand, itemStack1);
                }
                else {
                    ItemStack itemStack2 = itemStack.copy();
                    itemStack2.decrement(1);
                    player.setStackInHand(hand,itemStack2);
                    player.giveItemStack(itemStack1);
                }
                return ActionResult.SUCCESS;
            }
            else
                return ActionResult.FAIL;
        }
        else {
            if(!world.getBlockState(pos).isAir() && !fluid1.matchesType(fluid)) {
                return ActionResult.FAIL;
            }
            FluidPool fluidPool = new FluidPool(world,pos,fluid);
            if(fluidPool.addFluid(8)) {
                bucket.onEmptied(player, world, itemStack, pos);
                player.setStackInHand(hand, BucketItem.getEmptiedStack(itemStack, player));
                return ActionResult.SUCCESS;
            }
            else
                return ActionResult.FAIL;
        }
    }
}
