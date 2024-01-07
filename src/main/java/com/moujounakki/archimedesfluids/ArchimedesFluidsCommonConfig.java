package com.moujounakki.archimedesfluids;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class ArchimedesFluidsCommonConfig {
    public static void initialize() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.getRight());
    }
    private static final Pair<ArchimedesFluidsCommonConfig, ForgeConfigSpec> config = new ForgeConfigSpec.Builder().configure(ArchimedesFluidsCommonConfig::new);
    private final ConfigValue<Integer> maxUpdateQueueSize;
    private final ConfigValue<Integer> updateQueueCleanInterval;
    private final ConfigValue<Integer> maxUpdatesPerTick;
    private ArchimedesFluidsCommonConfig(ForgeConfigSpec.Builder builder) {
        maxUpdateQueueSize = builder.define("max_update_queue_size", 4096);
        updateQueueCleanInterval =  builder.define("update_queue_clean_interval", 256);
        maxUpdatesPerTick = builder.define("max_updates_per_tick", 128);
    }

    public static int getMaxUpdateQueueSize() {
        return config.getLeft().maxUpdateQueueSize.get();
    }
    public static int getUpdateQueueCleanInterval() {
        return config.getLeft().updateQueueCleanInterval.get();
    }
    public static int getMaxUpdatesPerTick() {
        return config.getLeft().maxUpdatesPerTick.get();
    }
}
