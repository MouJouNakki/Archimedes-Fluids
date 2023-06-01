package com.moujounakki.archimedesfluids;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class FluidCycleData extends SavedData {
    private int storedFluid = 0;
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putInt("storedFluid", storedFluid);
        return tag;
    }
    public static FluidCycleData create() {
        return new FluidCycleData();
    }

    public static FluidCycleData load(CompoundTag tag) {
        FluidCycleData data = create();
        data.storedFluid = tag.getInt("storedFluid");
        return data;
    }

    public boolean hasStoredFluid() {
        return storedFluid > 0;
    }

    public void modifyStoredFluid(int amount) {
        this.storedFluid = this.storedFluid+amount;
        this.setDirty();
    }

    private static final HashMap<ServerLevel, HashMap<String, FluidCycleData>> dataMap = new HashMap<>();
    public static FluidCycleData get(ServerLevel level, String fluid) {
        if(!dataMap.containsKey(level))
            dataMap.put(level, new HashMap<>());
        HashMap<String, FluidCycleData> dataMap2 = dataMap.get(level);
        if(dataMap2.containsKey(fluid)) {
            return dataMap2.get(fluid);
        }
        FluidCycleData data = level.getDataStorage().computeIfAbsent(FluidCycleData::load, FluidCycleData::create,"fluidCycle" + fluid);
        dataMap2.put(fluid, data);
        return data;
    }
}
