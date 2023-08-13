package com.moujounakki.archimedesfluids;

//import net.minecraft.core.Registry;
//import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.Property;
//import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//import net.minecraftforge.registries.ForgeRegistries;
//import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FluidloggingProperty extends Property<Fluidlogging> {
    public FluidloggingProperty() {
        super("fluidlogged", Fluidlogging.class);
    }
    private static Fluidlogging[] values;
    private static Fluidlogging[] getValues() {
        if(values == null) {
            values = new Fluidlogging[]{new Fluidlogging(Fluids.FLOWING_WATER, "9minecraftflowing_water"),
                    new Fluidlogging(Fluids.FLOWING_LAVA, "9minecraftflowing_lava"),
                    new Fluidlogging(Fluids.WATER, "9minecraftwater"),
                    new Fluidlogging(Fluids.LAVA, "9minecraftlava")};
        }
        return values;
    }
    public static Fluidlogging getFluidLogging(Fluid fluid) {
        for(Fluidlogging fluidlogging : getValues()) {
            if(fluidlogging.fluid == fluid)
                return fluidlogging;
        }
        return null;
    }
    @Override
    public Collection<Fluidlogging> getPossibleValues() {
        return List.of(FluidloggingProperty.getValues());
//        Collection<Fluidlogging> collection = new ArrayList<>();
//        for(Fluid fluid : ForgeRegistries.FLUIDS.getValues())
//            collection.add(new Fluidlogging(fluid));
//        return collection;
    }

    @Override
    public String getName(Fluidlogging fluidlogging) {
        return fluidlogging.name;
    }

    @Override
    public Optional<Fluidlogging> getValue(String string) {
        for(Fluidlogging fluidlogging : getValues()) {
            if(fluidlogging.name.equals(string))
                return Optional.of(fluidlogging);
        }
        return Optional.empty();
//        return Optional.of(new Fluidlogging(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(string))));
    }
}
