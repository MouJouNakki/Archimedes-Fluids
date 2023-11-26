package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FALLING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL;

public class FluidloggingData extends SavedData {
    private final HashMap<Fluid, HashMap<Integer, HashSet<BlockPos>>> storageMap = new HashMap<>();
    private static final HashMap<LevelAccessor, HashMap<ChunkPos, HashSet<BlockPos>>> chunkMap = new HashMap<>();
    private LevelAccessor levelAccessor;
    private static LevelAccessor lastAccessor;
    private static final HashMap<LevelAccessor, HashMap<BlockPos, FluidState>> stateMap = new HashMap<>();
    private static final HashMap<LevelAccessor, FluidloggingData> dataMap = new HashMap<>();
    public static FluidState getFluid(LevelAccessor level, BlockPos pos) {
        if(!stateMap.containsKey(level))
            return Fluids.EMPTY.defaultFluidState();
        HashMap<BlockPos, FluidState> levelMap = stateMap.get(level);
        if(!levelMap.containsKey(pos))
            return Fluids.EMPTY.defaultFluidState();
        return levelMap.get(pos);
    }
    public static void setFluid(LevelAccessor level, BlockPos pos, FluidState fluidstate) {
        ((IMixinBlockStateBase)level.getBlockState(pos)).setFluidloggingState(fluidstate);
        ChunkPos chunkPos = new ChunkPos(pos);
        boolean isEmptying = (fluidstate.isEmpty() || fluidstate.getAmount() < 1);
        if(level instanceof ServerLevel) {
            lastAccessor = level;
            getFromMap(dataMap, level, isEmptying, () -> (((ServerLevel) level).getDataStorage().computeIfAbsent(FluidloggingData::load, FluidloggingData::create, "fluidlogging")), (FluidloggingData data) -> getFromMap(data.storageMap, fluidstate.getType(), isEmptying, HashMap::new, (HashMap<Integer, HashSet<BlockPos>> amountMap) -> {
                HashSet<BlockPos> posSet = getFromMap(amountMap, fluidstate.getAmount(), isEmptying, HashSet::new);
                if(posSet == null)
                    return;
                posSet.add(pos);
                data.setDirty();
            }));
        }
        getFromMap(stateMap, level, isEmptying, HashMap::new, (HashMap<BlockPos, FluidState> posMap) -> posMap.put(pos, fluidstate));
    }

    private static <K, V> void getFromMap(HashMap<K, V> map, K key, boolean isEmptying, Supplier<V> ifMissing, Consumer<V> ifPresent) {
        V v = getFromMap(map, key, isEmptying, ifMissing);
        if(v == null)
            return;
        ifPresent.accept(v);
    }
    @Nullable
    private static <K, V> V getFromMap(HashMap<K, V> map, K key, boolean isEmptying, Supplier<V> ifMissing) {
        if(map.containsKey(key))
            return map.get(key);
        if(isEmptying)
            return null;
        V v = ifMissing.get();
        map.put(key, v);
        return v;
    }
    @Override
    public CompoundTag save(CompoundTag tag) {
        for(Fluid fluid : storageMap.keySet()) {
            CompoundTag fluidTag = new CompoundTag();
            for(Integer integer : storageMap.get(fluid).keySet()) {
                ListTag integerTag = new ListTag();
                for(BlockPos pos : storageMap.get(fluid).get(integer))
                    integerTag.add(StringTag.valueOf(pos.toShortString()));
                tag.put(integer.toString(), integerTag);
            }
            tag.put(Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).toString(), fluidTag);
        }
        return tag;
    }
    public static FluidloggingData create() {
        FluidloggingData data = new FluidloggingData();
        data.levelAccessor = lastAccessor;
        return data;
    }

    public static FluidloggingData load(CompoundTag tag) {
        FluidloggingData data = new FluidloggingData();
        data.levelAccessor = lastAccessor;
        for(String fluid : tag.getAllKeys()) {
            for(String integer : ((CompoundTag)(Objects.requireNonNull(tag.get(fluid)))).getAllKeys()) {
                for(Tag pos : (ListTag)Objects.requireNonNull(tag.get(integer))) {
                    String[] pos1 = pos.getAsString().split(", ");
                    FluidloggingData.setFluid(data.levelAccessor, new BlockPos(Integer.parseInt(pos1[0]), Integer.parseInt(pos1[1]), Integer.parseInt(pos1[2])), Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluid))).defaultFluidState().setValue(LEVEL, Integer.valueOf(integer)).setValue(FALLING, false));
                }
            }
        }
        return data;
    }
    public static void noteLevelLoad(LevelAccessor level) {
        if(!dataMap.containsKey(level)) {
            dataMap.put(level, ((ServerLevel) level).getDataStorage().computeIfAbsent(FluidloggingData::load, FluidloggingData::create, "fluidlogging"));
        }
    }
}
