package mcjty.lostcities.varia;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lighting optimization utilities inspired by Alfheim
 * 
 * Alfheim is a lighting engine replacement that optimizes light calculations
 * through deduplication and smart queuing. We adapt these concepts for
 * Dead Structures' chunk generation.
 * 
 * Key optimizations:
 * - Deduplicate lighting updates (same block = single calculation)
 * - High-speed queue for batch processing
 * - Skip unnecessary light updates during generation
 * - Defer lighting calculations when possible
 */
public class LightingOptimizer {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Deduplication tracking
    private static final ConcurrentHashMap<Long, BlockPos> pendingLightUpdates = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<BlockPos> lightUpdateQueue = new ConcurrentLinkedQueue<>();
    
    // Statistics
    private static final AtomicLong updatesSubmitted = new AtomicLong(0);
    private static final AtomicLong updatesDeduplicated = new AtomicLong(0);
    private static final AtomicLong updatesProcessed = new AtomicLong(0);
    
    // Configuration
    private static final int BATCH_SIZE = 64; // Process updates in batches
    private static final int MAX_QUEUE_SIZE = 4096;
    
    /**
     * Submit a light update with deduplication
     * Inspired by Alfheim's deduplicated lighting engine
     */
    public static void submitLightUpdate(Level level, BlockPos pos) {
        if (level == null || pos == null) return;
        
        updatesSubmitted.incrementAndGet();
        
        // Pack position into long for efficient deduplication
        long packed = AllocationOptimizer.packInts(pos.getX(), pos.getZ());
        
        // Check if this position already has a pending update
        BlockPos existing = pendingLightUpdates.putIfAbsent(packed, pos);
        if (existing != null) {
            // Duplicate update - skip it (Alfheim optimization)
            updatesDeduplicated.incrementAndGet();
            return;
        }
        
        // New update - add to queue
        if (lightUpdateQueue.size() < MAX_QUEUE_SIZE) {
            lightUpdateQueue.offer(pos);
        }
    }
    
    /**
     * Process a batch of light updates
     * Uses high-speed queue similar to Alfheim
     */
    public static int processBatchUpdates(Level level) {
        int processed = 0;
        
        for (int i = 0; i < BATCH_SIZE && !lightUpdateQueue.isEmpty(); i++) {
            BlockPos pos = lightUpdateQueue.poll();
            if (pos == null) break;
            
            // Remove from pending map
            long packed = AllocationOptimizer.packInts(pos.getX(), pos.getZ());
            pendingLightUpdates.remove(packed);
            
            // Process the light update
            try {
                level.getLightEngine().checkBlock(pos);
                processed++;
                updatesProcessed.incrementAndGet();
            } catch (Exception e) {
                LOGGER.warn("Failed to process light update at {}: {}", pos, e.getMessage());
            }
        }
        
        return processed;
    }
    
    /**
     * Check if lighting should be calculated during chunk generation
     * Skip if not necessary (performance optimization)
     */
    public static boolean shouldCalculateLighting(Level level, boolean isInitialGeneration) {
        // Skip lighting during initial worldgen (will be calculated later)
        if (isInitialGeneration) {
            return false;
        }
        
        // Skip if queue is overwhelmed
        if (lightUpdateQueue.size() > MAX_QUEUE_SIZE * 0.9) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Defer lighting calculation for a chunk
     * Marks chunk for later lighting processing
     */
    public static void deferChunkLighting(Level level, int chunkX, int chunkZ) {
        // Submit corners of chunk for lighting
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        
        // Submit strategic positions that will trigger chunk relighting
        submitLightUpdate(level, new BlockPos(baseX, 64, baseZ));
        submitLightUpdate(level, new BlockPos(baseX + 15, 64, baseZ));
        submitLightUpdate(level, new BlockPos(baseX, 64, baseZ + 15));
        submitLightUpdate(level, new BlockPos(baseX + 15, 64, baseZ + 15));
    }
    
    /**
     * Batch light update for multiple positions
     * More efficient than individual updates
     */
    public static void submitBatchUpdates(Level level, Iterable<BlockPos> positions) {
        for (BlockPos pos : positions) {
            submitLightUpdate(level, pos);
        }
    }
    
    /**
     * Clear all pending light updates
     */
    public static void clearPending() {
        int queueSize = lightUpdateQueue.size();
        int mapSize = pendingLightUpdates.size();
        
        lightUpdateQueue.clear();
        pendingLightUpdates.clear();
        
        if (queueSize > 0 || mapSize > 0) {
            LOGGER.debug("Cleared {} queued and {} pending light updates", queueSize, mapSize);
        }
    }
    
    /**
     * Get queue size
     */
    public static int getQueueSize() {
        return lightUpdateQueue.size();
    }
    
    /**
     * Get pending updates count
     */
    public static int getPendingCount() {
        return pendingLightUpdates.size();
    }
    
    /**
     * Check if light update queue is overwhelmed
     */
    public static boolean isQueueOverwhelmed() {
        return lightUpdateQueue.size() > MAX_QUEUE_SIZE * 0.75;
    }
    
    /**
     * Get deduplication ratio
     */
    public static double getDeduplicationRatio() {
        long submitted = updatesSubmitted.get();
        long deduplicated = updatesDeduplicated.get();
        
        if (submitted == 0) return 0.0;
        return (deduplicated * 100.0) / submitted;
    }
    
    /**
     * Get statistics
     */
    public static String getStats() {
        return String.format(
            "Lighting Optimizer - Submitted: %d, Deduplicated: %d (%.1f%%), Processed: %d, Queue: %d, Pending: %d",
            updatesSubmitted.get(),
            updatesDeduplicated.get(),
            getDeduplicationRatio(),
            updatesProcessed.get(),
            lightUpdateQueue.size(),
            pendingLightUpdates.size()
        );
    }
    
    /**
     * Reset statistics
     */
    public static void resetStats() {
        updatesSubmitted.set(0);
        updatesDeduplicated.set(0);
        updatesProcessed.set(0);
    }
    
    /**
     * Cleanup - called on world unload
     */
    public static void cleanup() {
        LOGGER.info("Cleaning up lighting optimizer");
        LOGGER.info(getStats());
        
        clearPending();
        resetStats();
    }
}
