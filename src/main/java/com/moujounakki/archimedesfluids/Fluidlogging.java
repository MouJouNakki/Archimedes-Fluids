package com.moujounakki.archimedesfluids;

//import net.minecraft.resources.ResourceLocation;
import net.minecraft.fluid.FlowableFluid;
//import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

public class Fluidlogging implements Comparable<Fluidlogging> {
    public final FlowableFluid fluid;
    public final String name;
    public Fluidlogging(FlowableFluid fluid, String name) {
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
