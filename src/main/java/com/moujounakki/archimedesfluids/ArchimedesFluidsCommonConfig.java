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
    private final ConfigValue<Boolean> partialBuckets;
    private final ConfigValue<Integer> flowSpeed;
    private final ConfigValue<Double> puddleSpread;
    private ArchimedesFluidsCommonConfig(ForgeConfigSpec.Builder builder) {
        partialBuckets = builder.define("partial_buckets", true);
        flowSpeed = builder.define("flow_speed", 1);
        puddleSpread = builder.define("puddle_spread", 0.3);
    }

    public static boolean getPartialBuckets() {
        return config.getLeft().partialBuckets.get();
    }
    public static int getFlowSpeed() {
        return config.getLeft().flowSpeed.get();
    }
    public static double getPuddleSpread() {
        return config.getLeft().puddleSpread.get();
    }
}
