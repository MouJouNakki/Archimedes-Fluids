package com.moujounakki.archimedesfluids;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class Fluidlogging implements Comparable<Fluidlogging> {
    public final Fluid fluid;
    public final String name;
    public Fluidlogging(Fluid fluid, String name) {
        this.fluid = fluid;
        this.name = name;
    }

//    public Fluidlogging(Fluid fluid) {
//        this.fluid = fluid;
//        ResourceLocation resourceLocation = ForgeRegistries.FLUIDS.getKey(fluid);
//        if(resourceLocation == null)
//            throw new RuntimeException("Unable to name unregistered fluid: " + fluid.toString());
//        this.name = resourceLocation.getNamespace().length() + resourceLocation.getNamespace() + resourceLocation.getPath();
//    }
    @Override
    public int compareTo(@NotNull Fluidlogging o) {
        return 0;
    }
}
