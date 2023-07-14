package com.moujounakki.archimedesfluids;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ArchimedesFluids.MODID)
@SuppressWarnings("unused")
public class ArchimedesFluids
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "archimedesfluids";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public ArchimedesFluids()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(!(event.getLevel() instanceof Level))
            return;
        FluidState state = event.getBlockSnapshot().getReplacedBlock().getFluidState();
        Fluid fluid = state.getType();
        if(fluid.isSame(Fluids.EMPTY))
            return;
        FluidPool fluidPool = new FluidPool((Level)event.getLevel(), event.getPos(), fluid);
        if(!fluidPool.addFluid(state.getAmount()))
            event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPistonMovePre(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof Level))
            return;
        PistonStructureResolver structureResolver = event.getStructureHelper();
        if(structureResolver == null)
            return;
        structureResolver.resolve();
        List<BlockPos> toDestroy = Objects.requireNonNull(structureResolver).getToDestroy();
        LevelAccessor level = event.getLevel();
        for (BlockPos pos : toDestroy) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.isEmpty()) {
                continue;
            }
            FluidPool fluidPool = new FluidPool((Level)level, pos, fluidState.getType());
            for (BlockPos pos1 : toDestroy) {
                fluidPool.setBanned(pos1);
            }
            if(!fluidPool.checkForSpace(fluidState.getAmount())) {
                event.setCanceled(true);
                return;
            }
        }
        for (BlockPos pos : toDestroy) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.isEmpty()) {
                continue;
            }
            FluidPool fluidPool = new FluidPool((Level)level, pos, fluidState.getType());
            for (BlockPos pos1 : toDestroy) {
                fluidPool.setBanned(pos1);
            }
            if(!fluidPool.addFluid(fluidState.getAmount())) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        Fluid fluid = ((BucketItem)(event.getEmptyBucket().getItem())).getFluid();
        Level level = event.getLevel();
        HitResult target = event.getTarget();
        if(target == null)
            return;
        BlockPos pos = new BlockPos(target.getLocation());
        Fluid fluid1 = level.getFluidState(pos).getType();
        if(fluid == Fluids.EMPTY) {
            if(fluid1 == Fluids.EMPTY) {
                event.setCanceled(true);
                return;
            }
            FluidPool fluidPool = new FluidPool(event.getLevel(),pos,fluid1);
            if(fluidPool.removeFluid(8)) {
                event.setResult(Event.Result.ALLOW);
            }
            else
                event.setCanceled(true);
        }
        else {
            if(!level.getBlockState(pos).isAir() && !fluid1.isSame(fluid)) {
                event.setCanceled(true);
                return;
            }
            FluidPool fluidPool = new FluidPool(event.getLevel(),pos,fluid);
            if(fluidPool.addFluid(8)) {
                event.setResult(Event.Result.ALLOW);
            }
            else
                event.setCanceled(true);
        }
    }
}
