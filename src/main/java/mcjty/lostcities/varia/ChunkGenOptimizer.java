package mcjty.lostcities.varia;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chunk generation optimization utilities inspired by C2ME (Concurrent Chunk Management Engine)
 * 
 * Key optimizations:
 * - Parallel chunk generation using ForkJoinPool
 * - Biome lookup caching to reduce redundant calculations
 * - Object pooling to reduce allocations
 * - Fast math operations for noise/terrain generation
 * - Task prioritization for better chunk loading order
 */
public class ChunkGenOptimizer {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Threading configuration
    private static final int THREAD_POOL_SIZE = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
    private static final ForkJoinPool CHUNK_GEN_POOL = new ForkJoinPool(
        THREAD_POOL_SIZE,
        ForkJoinPool.defaultForkJoinWorkerThreadFactory,
        null,
        true // Async mode for better throughput
    );
    
    // Biome cache for fast lookups (C2ME optimization)
    private static final int BIOME_CACHE_SIZE = 4096;
    private static final ConcurrentHashMap<Long, String> biomeCache = new ConcurrentHashMap<>(BIOME_CACHE_SIZE);
    private static final AtomicInteger biomeCacheHits = new AtomicInteger(0);
    private static final AtomicInteger biomeCacheMisses = new AtomicInteger(0);
    
    // Object pools to reduce allocations
    private static final ConcurrentLinkedQueue<double[]> noiseArrayPool = new ConcurrentLinkedQueue<>();
    private static final int MAX_POOLED_ARRAYS = 256;
    
    /**
     * Get the chunk generation thread pool
     */
    public static ForkJoinPool getChunkGenPool() {
        return CHUNK_GEN_POOL;
    }
    
    /**
     * Submit a chunk generation task to the parallel pool
     */
    public static <T> CompletableFuture<T> submitChunkTask(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                LOGGER.error("Chunk generation task failed", e);
                throw new RuntimeException(e);
            }
        }, CHUNK_GEN_POOL);
    }
    
    /**
     * Biome cache lookup - inspired by C2ME's biome cache optimization
     */
    public static String getCachedBiome(int x, int z, java.util.function.Supplier<String> biomeGetter) {
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
        
        String cached = biomeCache.get(key);
        if (cached != null) {
            biomeCacheHits.incrementAndGet();
            return cached;
        }
        
        // Cache miss - compute and cache
        biomeCacheMisses.incrementAndGet();
        String biome = biomeGetter.get();
        
        // Limit cache size
        if (biomeCache.size() < BIOME_CACHE_SIZE) {
            biomeCache.put(key, biome);
        } else {
            // Random eviction when full (fast, good enough for cache)
            if (Math.random() < 0.1) {
                biomeCache.clear();
            }
        }
        
        return biome;
    }
    
    /**
     * Get a pooled noise array to reduce allocations
     */
    public static double[] borrowNoiseArray(int size) {
        double[] array = noiseArrayPool.poll();
        if (array == null || array.length != size) {
            return new double[size];
        }
        return array;
    }
    
    /**
     * Return a noise array to the pool
     */
    public static void returnNoiseArray(double[] array) {
        if (array != null && noiseArrayPool.size() < MAX_POOLED_ARRAYS) {
            // Clear array before returning to pool
            java.util.Arrays.fill(array, 0.0);
            noiseArrayPool.offer(array);
        }
    }
    
    /**
     * Fast floor operation (C2ME math optimization)
     * Faster than Math.floor for positive and negative numbers
     */
    public static int fastFloor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }
    
    /**
     * Fast modulo operation for power-of-2 divisors
     */
    public static int fastMod(int value, int divisor) {
        // Only works for power of 2 divisors
        return value & (divisor - 1);
    }
    
    /**
     * Linear interpolation (lerp) - optimized version
     */
    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }
    
    /**
     * Bilinear interpolation for smooth terrain
     */
    public static double biLerp(double x, double y, double q00, double q10, double q01, double q11) {
        double r0 = lerp(x, q00, q10);
        double r1 = lerp(x, q01, q11);
        return lerp(y, r0, r1);
    }
    
    /**
     * Task priority for chunk loading
     * Higher priority = closer to player
     */
    public static class ChunkTask implements Comparable<ChunkTask> {
        private final int chunkX;
        private final int chunkZ;
        private final int priority;
        private final Runnable task;
        
        public ChunkTask(int chunkX, int chunkZ, int playerX, int playerZ, Runnable task) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.task = task;
            
            // Calculate priority based on distance to player
            int dx = chunkX - playerX;
            int dz = chunkZ - playerZ;
            this.priority = dx * dx + dz * dz; // Squared distance (no sqrt needed)
        }
        
        @Override
        public int compareTo(ChunkTask other) {
            // Lower distance = higher priority
            return Integer.compare(this.priority, other.priority);
        }
        
        public void run() {
            task.run();
        }
    }
    
    /**
     * Priority queue for chunk tasks (C2ME scheduling optimization)
     */
    private static final PriorityBlockingQueue<ChunkTask> taskQueue = new PriorityBlockingQueue<>();
    
    /**
     * Submit a prioritized chunk task
     */
    public static void submitPrioritizedTask(int chunkX, int chunkZ, int playerX, int playerZ, Runnable task) {
        taskQueue.offer(new ChunkTask(chunkX, chunkZ, playerX, playerZ, task));
    }
    
    /**
     * Process next highest-priority task
     */
    public static boolean processNextTask() {
        ChunkTask task = taskQueue.poll();
        if (task != null) {
            task.run();
            return true;
        }
        return false;
    }
    
    /**
     * Clear all caches and pools
     */
    public static void cleanup() {
        LOGGER.info("Cleaning up chunk generation optimizer");
        LOGGER.info("Biome cache stats - Hits: {}, Misses: {}, Size: {}", 
            biomeCacheHits.get(), biomeCacheMisses.get(), biomeCache.size());
        
        biomeCache.clear();
        noiseArrayPool.clear();
        taskQueue.clear();
        
        biomeCacheHits.set(0);
        biomeCacheMisses.set(0);
    }
    
    /**
     * Get performance statistics
     */
    public static String getStats() {
        int hits = biomeCacheHits.get();
        int misses = biomeCacheMisses.get();
        int total = hits + misses;
        double hitRate = total > 0 ? (hits * 100.0 / total) : 0;
        
        return String.format(
            "ChunkGen Stats - Threads: %d, Biome Cache: %d entries, Hit Rate: %.2f%%, Pooled Arrays: %d, Queued Tasks: %d",
            THREAD_POOL_SIZE, biomeCache.size(), hitRate, noiseArrayPool.size(), taskQueue.size()
        );
    }
    
    /**
     * Shutdown the thread pool gracefully
     */
    public static void shutdown() {
        LOGGER.info("Shutting down chunk generation thread pool");
        LOGGER.info(getStats());
        
        cleanup();
        
        CHUNK_GEN_POOL.shutdown();
        try {
            if (!CHUNK_GEN_POOL.awaitTermination(10, TimeUnit.SECONDS)) {
                CHUNK_GEN_POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            CHUNK_GEN_POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
