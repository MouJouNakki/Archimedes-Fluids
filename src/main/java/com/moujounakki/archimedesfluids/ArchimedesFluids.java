package com.moujounakki.archimedesfluids;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
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

import java.util.List;
import java.util.Objects;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ArchimedesFluids.MODID)
@SuppressWarnings("unused")
public class ArchimedesFluids {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "archimedesfluids";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final FluidloggingProperty FLUIDLOGGED = new FluidloggingProperty();
    public static final IntegerProperty FLUID_LEVEL = IntegerProperty.create("fluid_level", 0, 8);

    public ArchimedesFluids() {
        // Initialize the configuration
        ArchimedesFluidsCommonConfig.initialize();

        // Get the mod event bus
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(!(event.getLevel() instanceof Level))
            return;
        Block placing = event.getPlacedBlock().getBlock();
        if(placing instanceof LiquidBlockContainer && !(placing instanceof SimpleWaterloggedBlock))
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
        Vec3 location = target.getLocation();
        BlockPos pos = new BlockPos((int) Math.floor(location.x()), (int) Math.floor(location.y()), (int) Math.floor(location.z()));
        Fluid fluid1 = level.getFluidState(pos).getType();
        if(fluid == Fluids.EMPTY) {
            if(fluid1 == Fluids.EMPTY) {
                event.setCanceled(true);
                return;
            }
            FluidPool fluidPool = new FluidPool(event.getLevel(),pos,fluid1);
            if(fluidPool.removeFluid(8)) {
                event.setFilledBucket(new ItemStack(fluid1.getBucket()));
                event.setResult(Event.Result.ALLOW);
                ((BucketItem)(event.getEmptyBucket().getItem())).checkExtraContent(event.getEntity(), level, event.getEmptyBucket(), pos);
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
                event.setFilledBucket(new ItemStack(Items.BUCKET));
                event.setResult(Event.Result.ALLOW);
                ((BucketItem)(event.getEmptyBucket().getItem())).checkExtraContent(event.getEntity(), level, event.getEmptyBucket(), pos);
            }
            else
                event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        FluidTicker.getInstance(event.level).tick();
    }
}