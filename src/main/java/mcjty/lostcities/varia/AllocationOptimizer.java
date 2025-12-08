package mcjty.lostcities.varia;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Allocation reduction utilities inspired by C2ME's allocation optimizations
 * 
 * Reduces garbage collection pressure by:
 * - Object pooling for frequently created objects
 * - Primitive array reuse
 * - StringBuilder pooling
 * - Reducing temporary object creation
 */
public class AllocationOptimizer {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Tracking allocations saved
    private static final AtomicLong arraysReused = new AtomicLong(0);
    private static final AtomicLong buildersReused = new AtomicLong(0);
    private static final AtomicLong coordsReused = new AtomicLong(0);
    
    // Configuration
    private static final int MAX_POOL_SIZE = 512;
    
    /**
     * Thread-safe object pool
     */
    private static class ObjectPool<T> {
        private final ConcurrentLinkedQueue<T> pool = new ConcurrentLinkedQueue<>();
        private final java.util.function.Supplier<T> factory;
        private final java.util.function.Consumer<T> resetter;
        private final int maxSize;
        
        public ObjectPool(java.util.function.Supplier<T> factory, 
                         java.util.function.Consumer<T> resetter, 
                         int maxSize) {
            this.factory = factory;
            this.resetter = resetter;
            this.maxSize = maxSize;
        }
        
        public T acquire() {
            T obj = pool.poll();
            if (obj == null) {
                return factory.get();
            }
            return obj;
        }
        
        public void release(T obj) {
            if (obj != null && pool.size() < maxSize) {
                resetter.accept(obj);
                pool.offer(obj);
            }
        }
        
        public int size() {
            return pool.size();
        }
        
        public void clear() {
            pool.clear();
        }
    }
    
    /**
     * Pooled int array for chunk coordinates, heights, etc.
     */
    private static final ObjectPool<int[]> intArray16Pool = new ObjectPool<>(
        () -> new int[16],
        arr -> java.util.Arrays.fill(arr, 0),
        MAX_POOL_SIZE
    );
    
    private static final ObjectPool<int[]> intArray256Pool = new ObjectPool<>(
        () -> new int[256],
        arr -> java.util.Arrays.fill(arr, 0),
        MAX_POOL_SIZE
    );
    
    /**
     * Pooled double array for noise generation
     */
    private static final ObjectPool<double[]> doubleArray256Pool = new ObjectPool<>(
        () -> new double[256],
        arr -> java.util.Arrays.fill(arr, 0.0),
        MAX_POOL_SIZE
    );
    
    private static final ObjectPool<double[]> doubleArray1024Pool = new ObjectPool<>(
        () -> new double[1024],
        arr -> java.util.Arrays.fill(arr, 0.0),
        MAX_POOL_SIZE
    );
    
    /**
     * Pooled StringBuilder for string operations
     */
    private static final ObjectPool<StringBuilder> stringBuilderPool = new ObjectPool<>(
        () -> new StringBuilder(256),
        sb -> sb.setLength(0),
        MAX_POOL_SIZE / 4
    );
    
    /**
     * Pooled ChunkCoord to reduce allocations
     */
    public static class PooledChunkCoord {
        public int x;
        public int z;
        
        public PooledChunkCoord() {
            this(0, 0);
        }
        
        public PooledChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        public void set(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        public long toLong() {
            return ((long) x << 32) | (z & 0xFFFFFFFFL);
        }
        
        @Override
        public int hashCode() {
            return 31 * x + z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PooledChunkCoord)) return false;
            PooledChunkCoord other = (PooledChunkCoord) obj;
            return this.x == other.x && this.z == other.z;
        }
    }
    
    private static final ObjectPool<PooledChunkCoord> chunkCoordPool = new ObjectPool<>(
        PooledChunkCoord::new,
        coord -> coord.set(0, 0),
        MAX_POOL_SIZE
    );
    
    /**
     * Acquire a pooled int array (size 16)
     */
    public static int[] acquireIntArray16() {
        arraysReused.incrementAndGet();
        return intArray16Pool.acquire();
    }
    
    /**
     * Release a pooled int array (size 16)
     */
    public static void releaseIntArray16(int[] array) {
        intArray16Pool.release(array);
    }
    
    /**
     * Acquire a pooled int array (size 256)
     */
    public static int[] acquireIntArray256() {
        arraysReused.incrementAndGet();
        return intArray256Pool.acquire();
    }
    
    /**
     * Release a pooled int array (size 256)
     */
    public static void releaseIntArray256(int[] array) {
        intArray256Pool.release(array);
    }
    
    /**
     * Acquire a pooled double array (size 256)
     */
    public static double[] acquireDoubleArray256() {
        arraysReused.incrementAndGet();
        return doubleArray256Pool.acquire();
    }
    
    /**
     * Release a pooled double array (size 256)
     */
    public static void releaseDoubleArray256(double[] array) {
        doubleArray256Pool.release(array);
    }
    
    /**
     * Acquire a pooled double array (size 1024)
     */
    public static double[] acquireDoubleArray1024() {
        arraysReused.incrementAndGet();
        return doubleArray1024Pool.acquire();
    }
    
    /**
     * Release a pooled double array (size 1024)
     */
    public static void releaseDoubleArray1024(double[] array) {
        doubleArray1024Pool.release(array);
    }
    
    /**
     * Acquire a pooled StringBuilder
     */
    public static StringBuilder acquireStringBuilder() {
        buildersReused.incrementAndGet();
        return stringBuilderPool.acquire();
    }
    
    /**
     * Release a pooled StringBuilder
     */
    public static void releaseStringBuilder(StringBuilder sb) {
        stringBuilderPool.release(sb);
    }
    
    /**
     * Acquire a pooled ChunkCoord
     */
    public static PooledChunkCoord acquireChunkCoord(int x, int z) {
        coordsReused.incrementAndGet();
        PooledChunkCoord coord = chunkCoordPool.acquire();
        coord.set(x, z);
        return coord;
    }
    
    /**
     * Release a pooled ChunkCoord
     */
    public static void releaseChunkCoord(PooledChunkCoord coord) {
        chunkCoordPool.release(coord);
    }
    
    /**
     * Fast hash combining for coordinates
     * Reduces hashCode allocations
     */
    public static int hashCoords(int x, int z) {
        return 31 * x + z;
    }
    
    /**
     * Fast hash combining for 3D coordinates
     */
    public static int hashCoords(int x, int y, int z) {
        int hash = x;
        hash = 31 * hash + y;
        hash = 31 * hash + z;
        return hash;
    }
    
    /**
     * Pack two ints into a long for efficient storage
     */
    public static long packInts(int a, int b) {
        return ((long) a << 32) | (b & 0xFFFFFFFFL);
    }
    
    /**
     * Unpack first int from packed long
     */
    public static int unpackFirst(long packed) {
        return (int) (packed >> 32);
    }
    
    /**
     * Unpack second int from packed long
     */
    public static int unpackSecond(long packed) {
        return (int) packed;
    }
    
    /**
     * Get pool statistics
     */
    public static String getStats() {
        return String.format(
            "Allocation Optimizer - Arrays Reused: %d, Builders Reused: %d, Coords Reused: %d | " +
            "Pools: Int16=%d, Int256=%d, Double256=%d, Double1024=%d, SB=%d, Coord=%d",
            arraysReused.get(), buildersReused.get(), coordsReused.get(),
            intArray16Pool.size(), intArray256Pool.size(), 
            doubleArray256Pool.size(), doubleArray1024Pool.size(),
            stringBuilderPool.size(), chunkCoordPool.size()
        );
    }
    
    /**
     * Clear all pools
     */
    public static void cleanup() {
        LOGGER.info("Cleaning up allocation optimizer");
        LOGGER.info(getStats());
        
        intArray16Pool.clear();
        intArray256Pool.clear();
        doubleArray256Pool.clear();
        doubleArray1024Pool.clear();
        stringBuilderPool.clear();
        chunkCoordPool.clear();
        
        arraysReused.set(0);
        buildersReused.set(0);
        coordsReused.set(0);
    }
}
