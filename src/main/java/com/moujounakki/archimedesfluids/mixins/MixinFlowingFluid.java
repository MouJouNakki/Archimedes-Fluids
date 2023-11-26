package com.moujounakki.archimedesfluids.mixins;

import com.moujounakki.archimedesfluids.*;
import java.util.List;
import java.util.Collections;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Arrays;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(FlowingFluid.class)
@SuppressWarnings("unused")
public abstract class MixinFlowingFluid extends Fluid implements IMixinFlowingFluid {
    private final Queue<BlockPos> updateQueue = new ArrayDeque<>();

public void tick(Level level, BlockPos pos, FluidState state) {
    BlockState blockstate = level.getBlockState(pos.below());
    FluidState fluidstate = blockstate.getFluidState();
    FluidSpreadType spreadType = this.getFluidSpreadType(blockstate);
    int amount = this.getAmount(state);

    if (spreadType == FluidSpreadType.REPLACE) {
        if (!blockstate.isAir()) {
            this.beforeDestroyingBlock(level, pos.below(), blockstate);
        }
        this.transferFluid(level, pos, pos.below(), amount);
    } else if (spreadType == FluidSpreadType.ADD && fluidstate.getAmount() < 8) {
        int otherAmount = fluidstate.getAmount();
        int transfer = Math.min(amount, 8 - otherAmount);
        this.transferFluid(level, pos, pos.below(), transfer);
    } else if (amount > 1) {
        List<Direction> directionList = Direction.Plane.HORIZONTAL.shuffledCopy(level.getRandom());
        Direction[] shuffledDirections = directionList.toArray(new Direction[0]);
        for (Direction direction : shuffledDirections) {
            BlockPos pos1 = pos.relative(direction);
            BlockState blockstate1 = level.getBlockState(pos1);
            FluidState fluidstate1 = blockstate1.getFluidState();
            FluidSpreadType spreadType1 = this.getFluidSpreadType(blockstate1);

            if (spreadType1 == FluidSpreadType.REPLACE) {
                if (!blockstate1.isAir()) {
                    this.beforeDestroyingBlock(level, pos1, blockstate1);
                }
                this.transferFluid(level, pos, pos1);
                break;
            } else if (spreadType1 == FluidSpreadType.ADD) {
                int otherAmount = fluidstate1.getAmount();
                if (amount > otherAmount) {
                    this.transferFluid(level, pos, pos1);
                    break;
                }
            }
        }
    } else if (amount == 1 && level.random.nextFloat() < 0.3) {
        List<Direction> directionList = Direction.Plane.HORIZONTAL.shuffledCopy(level.getRandom());
        Direction[] shuffledDirections = directionList.toArray(new Direction[0]);
        for (Direction direction : shuffledDirections) {
            BlockPos pos1 = pos.relative(direction);
            BlockState blockstate1 = level.getBlockState(pos1);
            FluidSpreadType spreadType1 = this.getFluidSpreadType(blockstate1);

            if (spreadType1 == FluidSpreadType.REPLACE) {
                if (!blockstate1.isAir()) {
                    this.beforeDestroyingBlock(level, pos1, blockstate1);
                }
                this.transferFluid(level, pos, pos1);
                break;
            }
        }
    }

    // Get the configuration
    ArchimedesFluidsConfig config = ArchimedesFluidsConfig.getInstance();
    int maxUpdatesPerTick = config.getMaxUpdatesPerTick();
    int queueCleanInterval = config.getQueueCleanInterval();

    // Reset the updates processed counter for each tick
    int updatesProcessed = 0;

    // Process updates from the queue, up to the specified limit.
    while (updatesProcessed < maxUpdatesPerTick && !updateQueue.isEmpty()) {
        BlockPos updatePos = updateQueue.poll();
        // Implement your custom logic here for processing updates from the queue
        updatesProcessed++;
    }

    // Enqueue the current position for future processing only if the queue size is below the configured limit
    if (updateQueue.size() < config.getMaxQueueSize()) {
        updateQueue.offer(pos);
    } else {
        // Optionally, you can log a message or take some action when the queue is full
        // For example, you can skip adding to the queue or remove old entries
    }

    // Optionally clean the queue at regular intervals
    if (queueCleanInterval > 0 && level.getGameTime() % queueCleanInterval == 0) {
        cleanQueue();  // Implement this method based on your queue cleaning logic
    }
}

private void cleanQueue() {
    final int MAX_QUEUE_SIZE = ArchimedesFluidsConfig.getInstance().getMaxQueueSize(); // Get the max queue size from the config

    // Limit the size of the queue
    while (updateQueue.size() > MAX_QUEUE_SIZE) {
        updateQueue.poll(); // Remove the oldest elements to maintain the size limit
    }

    // Additional cleaning logic can be added here if needed
    // For example, you might want to remove positions that are no longer relevant for updates
    // This might depend on the specific logic of your fluid dynamics and the state of the world
}

    private boolean isFallingAt(LevelReader reader, BlockPos pos) {
        BlockState blockstate = reader.getBlockState(pos);
        BlockPos blockpos1 = pos.above();
        BlockState blockstate2 = reader.getBlockState(blockpos1);
        FluidState fluidstate2 = blockstate2.getFluidState();
        
        return (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.canPassThroughWall(Direction.UP, reader, pos, blockstate, blockpos1, blockstate2));
    }
    
    @SuppressWarnings("SameReturnValue")
    @Shadow
    private boolean canPassThroughWall(Direction p_76062_, BlockGetter p_76063_, BlockPos p_76064_, BlockState p_76065_, BlockPos p_76066_, BlockState p_76067_) {
        return false;
    }
    
    private void transferFluid(LevelAccessor level, BlockPos from, BlockPos to) {
        this.transferFluid(level, from, to, level.getFluidState(from).getAmount(), level.getFluidState(to).getAmount());
    }
    
    private void transferFluid(LevelAccessor level, BlockPos from, BlockPos to, int transfer) {
        FluidState fromFluidState = level.getFluidState(from);
        FluidState toFluidState = level.getFluidState(to);
        this.transferFluid(level, from, to, fromFluidState.getAmount(), toFluidState.getAmount(), transfer);
    }
    
    private void transferFluid(LevelAccessor level, BlockPos from, BlockPos to, int fromAmount, int toAmount) {
        this.transferFluid(level, from, to, fromAmount, toAmount, 1);
    }
    
    private void transferFluid(LevelAccessor level, BlockPos from, BlockPos to, int fromAmount, int toAmount, int transfer) {
        this.setFlowing(level, to, toAmount + transfer, to.getY());
        this.setFlowing(level, from, fromAmount - transfer, from.getY());
    }

private void setFlowing(LevelAccessor level, BlockPos pos, int amount, int sourceYLevel) {
    FluidState fluidState = level.getFluidState(pos);
    if (fluidState.getType() == Fluids.WATER) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        List<ResourceKey<Biome>> desiredBiomes = Arrays.asList(
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "ocean")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "deep_ocean")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "warm_ocean")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "deep_warm_ocean")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "lukewarm_ocean")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "deep_lukewarm_ocean")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "cold_ocean")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "deep_cold_ocean")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "frozen_ocean")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "deep_frozen_ocean"))
        );

        int yLevel = pos.getY();
        int seaLevel = level.getSeaLevel();
        if (yLevel == sourceYLevel && desiredBiomes.stream().anyMatch(biomeHolder::is) && yLevel >= seaLevel - 2 && yLevel <= seaLevel) {
            amount = 8;
        }
    }

    BlockState blockState = level.getBlockState(pos);
    if (blockState.hasProperty(ArchimedesFluids.FLUID_LEVEL)) {
        Fluidlogging fluidlogging = FluidloggingProperty.getFluidLogging(fluidState.getType());
        if (fluidlogging != null) {
            level.setBlock(pos, blockState
                .setValue(ArchimedesFluids.FLUIDLOGGED, fluidlogging)
                .setValue(ArchimedesFluids.FLUID_LEVEL, amount)
                .setValue(BlockStateProperties.WATERLOGGED, false), 3);
            level.scheduleTick(pos, this, this.getTickDelay(level));
            return;
        }
    }

    if (amount < 1) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    } else {
        level.setBlock(pos, this.getFlowing(amount, this.isFallingAt(level, pos)).createLegacyBlock(), 3);
    }
}
 
    @Override
    public void changeFluid(Level level, BlockPos pos, int amount) {
        int current = level.getFluidState(pos).getAmount();
        
        if (current + amount > 8) {
            throw new IllegalArgumentException(String.format("Cannot add over 8 fluid (current %d, trying to add: %d)", current, amount));
        }
        
        this.setFlowing(level, pos, current + amount, pos.getY());
    }
    
    @Shadow
    public abstract FluidState getFlowing(int p_75954_, boolean p_75955_);
    
       @Shadow
    protected abstract void beforeDestroyingBlock(LevelAccessor p_76002_, BlockPos p_76003_, BlockState p_76004_);

    @Overwrite
    protected static int getLegacyLevel(FluidState p_76093_) {
        return 8 - Math.min(p_76093_.getAmount(), 8);
    }

    private FluidSpreadType getFluidSpreadType(BlockState blockstate) {
        FluidState fluidstate = blockstate.getFluidState();
        if (blockstate.canBeReplaced(this) && fluidstate.isEmpty()) {
            return FluidSpreadType.REPLACE;
        } else if (fluidstate.getType().isSame(this)) {
            return FluidSpreadType.ADD;
        } else if (fluidstate.isEmpty() && blockstate.hasProperty(ArchimedesFluids.FLUID_LEVEL)) {
            return FluidSpreadType.ADD;
        }
        return FluidSpreadType.BLOCKED;
    }

    @Overwrite
    public float getHeight(FluidState state, BlockGetter blockGetter, BlockPos pos) {
        if (hasSameAbove(state, blockGetter, pos)) {
            return 1.0F;
        }
        
        int found = 0;
        int amount = state.getAmount();
        
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            FluidState state1 = blockGetter.getFluidState(pos.relative(direction));
            
            if (!state1.getType().isSame(this)) {
                continue;
            }
            
            if (state1.getAmount() != amount + 1) {
                continue;
            }
            
            found++;
            if (found == 2) {
                break;
            }
        }
        
        if (found == 2) {
            return (amount + 1) / 9.0F;
        }
        
        return state.getOwnHeight();
    }

    @Overwrite
    public Vec3 getFlow(BlockGetter blockGetter, BlockPos pos, FluidState state) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        float stateHeight = state.getType().getHeight(state, blockGetter, pos);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            blockpos$mutableblockpos.setWithOffset(pos, direction);
            FluidState fluidstate = blockGetter.getFluidState(blockpos$mutableblockpos);
            
            if (this.affectsFlow(fluidstate)) {
                float f = fluidstate.getType().getHeight(fluidstate, blockGetter, blockpos$mutableblockpos);
                float f1 = 0.0F;
                
                if (f == 0.0F) {
                    if (!blockGetter.getBlockState(blockpos$mutableblockpos).blocksMotion()) {
                        BlockPos blockpos = blockpos$mutableblockpos.below();
                        FluidState fluidstate1 = blockGetter.getFluidState(blockpos);
                        
                        if (this.affectsFlow(fluidstate1)) {
                            f = fluidstate1.getType().getHeight(fluidstate1, blockGetter, blockpos);
                            if (f > 0.0F) {
                                f1 = stateHeight - (f - 0.8888889F);
                            }
                        }
                    }
                } else if (f > 0.0F) {
                    f1 = stateHeight - f;
                }

                if (f1 != 0.0F) {
                    d0 += (double)((float)direction.getStepX() * f1);
                    d1 += (double)((float)direction.getStepZ() * f1);
                }
            }
        }

        Vec3 vec3 = new Vec3(d0, 0.0D, d1);
        
        if (state.getValue(FALLING)) {
            for (Direction direction1 : Direction.Plane.HORIZONTAL) {
                blockpos$mutableblockpos.setWithOffset(pos, direction1);
                if (this.isSolidFace(blockGetter, blockpos$mutableblockpos, direction1) || this.isSolidFace(blockGetter, blockpos$mutableblockpos.above(), direction1)) {
                    vec3 = vec3.normalize().add(0.0D, -6.0D, 0.0D);
                    break;
                }
            }
        }

        return vec3.normalize();
    }

    @Shadow
    protected abstract boolean isSolidFace(BlockGetter p_75991_, BlockPos p_75992_, Direction p_75993_);

    @Shadow
    private boolean affectsFlow(FluidState p_76095_) {
        return false;
    }

    @Shadow
    private static boolean hasSameAbove(FluidState p_76089_, BlockGetter p_76090_, BlockPos p_76091_) {
        return false;
    }

    @Shadow
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
}
