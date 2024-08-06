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
    private final ConfigValue<Integer> farmlandWaterConsumption;
    //private final ConfigValue<Boolean> fluidlogging;
    private ArchimedesFluidsCommonConfig(ForgeConfigSpec.Builder builder) {
        partialBuckets = builder.define("partial_buckets", true);
        flowSpeed = builder.define("flow_speed", 1);
        puddleSpread = builder.define("puddle_spread", 0.3);
        farmlandWaterConsumption = builder.comment("Set to 0 for vanilla behaviour").define("farmland_water_consumption", 7);
        //fluidlogging = builder.comment("Can significantly slow down loading, especially when using many mods.", "May not load at all. ").define("fluidlogging", false);
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
    public static int getFarmlandWaterConsumption() {return config.getLeft().farmlandWaterConsumption.get();}
    public static boolean getFluidlogging() {
        return false;
        //return config.getLeft().fluidlogging.get();
    }
}
