package com.moujounakki.archimedesfluids;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ArchimedesFluids.MODID)
@SuppressWarnings({"unused", "SpellCheckingInspection"})
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

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        else if (event.side != LogicalSide.SERVER)
            return;
        Level level = event.level;
        if(!level.dimensionType().hasSkyLight())
            return;
        try {
            Class<ChunkMap> cls = ChunkMap.class;
            Field field = cls.getDeclaredField("visibleChunkMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked") ObjectCollection<ChunkHolder> collection = ((Long2ObjectLinkedOpenHashMap<ChunkHolder>)(field.get(((ServerLevel)level).getChunkSource().chunkMap))).values();
            int size = collection.size();
            if(size == 0)
                return;
            ChunkHolder[] visibleChunkMap = new ChunkHolder[size];
            visibleChunkMap = collection.toArray(visibleChunkMap);
            FluidCycleData data = FluidCycleData.get((ServerLevel) level, "Water");
            for(@SuppressWarnings("IntegerDivisionInFloatingPointContext") int i = (int)(Math.ceil(size/16)); i > 0; i--) {
                ChunkHolder holder = visibleChunkMap[(int)(Math.floor(Math.random()*size))];
                ChunkPos pos = holder.getPos();
                int x = pos.getBlockX((int)Math.floor(Math.random()*16));
                int z = pos.getBlockZ((int)Math.floor(Math.random()*16));
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x,z);
                BlockPos blockpos = new BlockPos(x,y,z);
                FluidState state = level.getFluidState(blockpos.below());
                Biome biome = level.getBiome(blockpos).value();
                if (level.isRaining() && biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.warmEnoughToRain(blockpos)) {
                    if(data.hasStoredFluid()) {
                        if(((IMixinFlowingFluid) Fluids.WATER).rainFluid(level, blockpos)) {
                            data.modifyStoredFluid(-1);
                        }
                    }
                }
                else if (state.getType().isSame(Fluids.WATER)) {
                    ((IMixinFlowingFluid) state.getType()).evaporateFluid(level, blockpos, state);
                    data.modifyStoredFluid(1);
                }
            }
        }
        catch (Throwable error) {
            //noinspection ThrowablePrintedToSystemOut
            System.err.println(error);
        }
    }
}
