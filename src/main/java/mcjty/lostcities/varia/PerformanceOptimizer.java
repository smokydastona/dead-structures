package mcjty.lostcities.varia;

import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance optimization utilities for Lost Cities
 * Based on techniques from Fabulously Optimized and Forge best practices
 */
public class PerformanceOptimizer {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Performance metrics
    private static final AtomicLong cacheHits = new AtomicLong(0);
    private static final AtomicLong cacheMisses = new AtomicLong(0);
    private static final AtomicLong cacheEvictions = new AtomicLong(0);
    
    // Cache configuration
    private static final int MAX_CACHE_SIZE = 8192; // Maximum cached chunks
    private static final int CLEANUP_THRESHOLD = 6144; // When to start cleanup (75%)
    private static final long CACHE_RETENTION_TIME = 60000; // 1 minute in milliseconds
    
    /**
     * Soft reference cache entry with timestamp for LRU eviction
     */
    public static class CacheEntry<T> {
        private final SoftReference<T> reference;
        private volatile long lastAccessTime;
        
        public CacheEntry(T value) {
            this.reference = new SoftReference<>(value);
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        public T get() {
            T value = reference.get();
            if (value != null) {
                lastAccessTime = System.currentTimeMillis();
            }
            return value;
        }
        
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        public boolean isStale() {
            return System.currentTimeMillis() - lastAccessTime > CACHE_RETENTION_TIME;
        }
    }
    
    /**
     * Thread-safe LRU cache with size limits and automatic cleanup
     */
    public static class LRUCache<K, V> {
        private final ConcurrentHashMap<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
        private final String cacheName;
        
        public LRUCache(String name) {
            this.cacheName = name;
        }
        
        public V get(K key) {
            CacheEntry<V> entry = cache.get(key);
            if (entry != null) {
                V value = entry.get();
                if (value != null) {
                    cacheHits.incrementAndGet();
                    return value;
                } else {
                    // Soft reference was cleared
                    cache.remove(key);
                    cacheEvictions.incrementAndGet();
                }
            }
            cacheMisses.incrementAndGet();
            return null;
        }
        
        public void put(K key, V value) {
            if (value == null) return;
            
            // Trigger cleanup if needed
            if (cache.size() >= CLEANUP_THRESHOLD) {
                cleanupStaleEntries();
            }
            
            cache.put(key, new CacheEntry<>(value));
        }
        
        public boolean containsKey(K key) {
            CacheEntry<V> entry = cache.get(key);
            return entry != null && entry.get() != null;
        }
        
        public void remove(K key) {
            cache.remove(key);
        }
        
        public void clear() {
            int size = cache.size();
            cache.clear();
            if (size > 0) {
                LOGGER.debug("Cleared {} cache: {} entries", cacheName, size);
            }
        }
        
        public int size() {
            return cache.size();
        }
        
        /**
         * Remove stale and garbage collected entries
         */
        public void cleanupStaleEntries() {
            long now = System.currentTimeMillis();
            AtomicInteger removed = new AtomicInteger(0);
            
            // Remove stale entries
            cache.entrySet().removeIf(entry -> {
                CacheEntry<V> cacheEntry = entry.getValue();
                boolean shouldRemove = cacheEntry.get() == null || cacheEntry.isStale();
                if (shouldRemove) removed.incrementAndGet();
                return shouldRemove;
            });
            
            // If still too large, remove oldest entries
            if (cache.size() > MAX_CACHE_SIZE) {
                cache.entrySet().stream()
                    .sorted((a, b) -> Long.compare(
                        a.getValue().getLastAccessTime(),
                        b.getValue().getLastAccessTime()
                    ))
                    .limit(cache.size() - MAX_CACHE_SIZE)
                    .forEach(entry -> {
                        cache.remove(entry.getKey());
                        removed.incrementAndGet();
                    });
            }
            
            if (removed.get() > 0) {
                cacheEvictions.addAndGet(removed.get());
                LOGGER.debug("Cleaned up {} cache: removed {} stale entries, {} remaining",
                    cacheName, removed.get(), cache.size());
            }
        }
    }
    
    /**
     * Block state pool to reuse common block states
     */
    public static class BlockStatePool {
        private static final ConcurrentHashMap<BlockState, BlockState> POOL = new ConcurrentHashMap<>();
        
        /**
         * Get a pooled instance of this block state
         */
        public static BlockState intern(BlockState state) {
            if (state == null) return null;
            return POOL.computeIfAbsent(state, k -> k);
        }
        
        /**
         * Clear the pool (call on dimension unload)
         */
        public static void clear() {
            int size = POOL.size();
            POOL.clear();
            if (size > 0) {
                LOGGER.debug("Cleared block state pool: {} unique states", size);
            }
        }
        
        public static int size() {
            return POOL.size();
        }
    }
    
    /**
     * Chunk unload event handler - cleanup caches when chunks unload
     */
    public static void onChunkUnload(ChunkCoord coord) {
        // This will be called from the event handler
        // Individual caches should implement cleanup
    }
    
    /**
     * Get cache statistics
     */
    public static String getCacheStats() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        double hitRate = total > 0 ? (hits * 100.0 / total) : 0;
        
        return String.format(
            "Cache Stats - Hits: %d, Misses: %d, Hit Rate: %.2f%%, Evictions: %d, Block Pool: %d",
            hits, misses, hitRate, cacheEvictions.get(), BlockStatePool.size()
        );
    }
    
    /**
     * Reset statistics (useful for testing)
     */
    public static void resetStats() {
        cacheHits.set(0);
        cacheMisses.set(0);
        cacheEvictions.set(0);
    }
    
    /**
     * Full cleanup - called on world unload or restart
     */
    public static void fullCleanup() {
        LOGGER.info("Performing full cache cleanup");
        LOGGER.info(getCacheStats());
        BlockStatePool.clear();
        resetStats();
    }
}
