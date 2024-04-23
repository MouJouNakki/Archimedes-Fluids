package com.moujounakki.archimedesfluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

public class FluidTicker {
    private final Level level;
    private static final HashMap<Level, FluidTicker> allFluidTickers = new HashMap<>();
    private final Queue<BlockPos> updateQueue = new ArrayDeque<>();
    public FluidTicker(Level level) {
        this.level = level;
    }
    public static FluidTicker getInstance(Level level) {
        if (!allFluidTickers.containsKey(level))
            allFluidTickers.put(level, new FluidTicker(level));
        return allFluidTickers.get(level);
    }
    public void scheduleTick(BlockPos pos) {
        // Enqueue the current position for future processing only if the queue size is below the configured limit
        if (updateQueue.size() < ArchimedesFluidsCommonConfig.getMaxUpdateQueueSize()) {
            updateQueue.offer(pos);
        } else {
            // Optionally, you can log a message or take some action when the queue is full
            // For example, you can skip adding to the queue or remove old entries
        }
    }
    public void tick()
    {
        // Get the configuration
        int maxUpdatesPerTick = ArchimedesFluidsCommonConfig.getMaxUpdatesPerTick();
        int queueCleanInterval = ArchimedesFluidsCommonConfig.getUpdateQueueCleanInterval();

        // Reset the updates processed counter for each tick
        int updatesProcessed = 0;

        // Process updates from the queue, up to the specified limit.
        while (updatesProcessed < maxUpdatesPerTick && !updateQueue.isEmpty()) {
            BlockPos updatePos = updateQueue.poll();
            FluidState fluidstate = level.getFluidState(updatePos);
            if (fluidstate.isEmpty())
                continue;
            ((IMixinFlowingFluid)(fluidstate.getType())).performTick(level, updatePos, fluidstate);
            updatesProcessed++;
        }

        // Optionally clean the queue at regular intervals
        if (queueCleanInterval > 0 && level.getGameTime() % queueCleanInterval == 0) {
            cleanQueue();  // Implement this method based on your queue cleaning logic
        }
    }
    private void cleanQueue() {
        final int MAX_QUEUE_SIZE = ArchimedesFluidsCommonConfig.getMaxUpdateQueueSize(); // Get the max queue size from the config

        // Limit the size of the queue
        while (updateQueue.size() > MAX_QUEUE_SIZE) {
            updateQueue.poll(); // Remove the oldest elements to maintain the size limit
        }

        // Additional cleaning logic can be added here if needed
        // For example, you might want to remove positions that are no longer relevant for updates
        // This might depend on the specific logic of your fluid dynamics and the state of the world
    }
}
