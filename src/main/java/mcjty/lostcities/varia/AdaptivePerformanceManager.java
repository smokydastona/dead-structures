package mcjty.lostcities.varia;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptive Performance Manager - Inspired by Dynamic FPS
 * 
 * Dynamically adjusts worldgen performance based on server state:
 * - Reduces chunk generation priority when no players nearby
 * - Scales processing based on server TPS
 * - Skips unnecessary calculations when chunks aren't visible
 * - Monitors server load and adjusts accordingly
 * 
 * Similar to how Dynamic FPS reduces client resources when idle,
 * this reduces server worldgen resources when appropriate.
 */
public class AdaptivePerformanceManager {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Performance states
    public enum PerformanceMode {
        FULL_SPEED,      // Normal operation (players actively exploring)
        REDUCED,         // Reduced processing (players idle/far away)
        MINIMAL,         // Minimal processing (no players nearby)
        BATTERY_SAVE     // Ultra-low processing (server under load)
    }
    
    // Configuration
    private static final int PLAYER_ACTIVITY_CHECK_RADIUS = 256; // blocks
    private static final long IDLE_THRESHOLD_MS = 60000; // 1 minute
    private static final long CHUNK_IDLE_THRESHOLD_MS = 30000; // 30 seconds
    private static final double LOW_TPS_THRESHOLD = 15.0;
    private static final double CRITICAL_TPS_THRESHOLD = 10.0;
    
    // State tracking
    private static PerformanceMode currentMode = PerformanceMode.FULL_SPEED;
    private static final AtomicLong lastPlayerActivity = new AtomicLong(System.currentTimeMillis());
    private static final AtomicBoolean serverUnderLoad = new AtomicBoolean(false);
    
    // Statistics
    private static final AtomicLong chunksSkipped = new AtomicLong(0);
    private static final AtomicLong calculationsReduced = new AtomicLong(0);
    
    /**
     * Check if a chunk should be generated based on performance mode
     */
    public static boolean shouldGenerateChunk(ChunkPos pos, ServerLevel level) {
        updatePerformanceMode(level);
        
        switch (currentMode) {
            case FULL_SPEED:
                return true;
                
            case REDUCED:
                // Generate only chunks within render distance of players
                if (!hasNearbyPlayers(pos, level, 16)) {
                    chunksSkipped.incrementAndGet();
                    return false;
                }
                return true;
                
            case MINIMAL:
                // Generate only chunks very close to players
                if (!hasNearbyPlayers(pos, level, 8)) {
                    chunksSkipped.incrementAndGet();
                    return false;
                }
                return true;
                
            case BATTERY_SAVE:
                // Generate only chunks immediately around players
                if (!hasNearbyPlayers(pos, level, 4)) {
                    chunksSkipped.incrementAndGet();
                    return false;
                }
                return true;
                
            default:
                return true;
        }
    }
    
    /**
     * Check if expensive calculations should be performed
     */
    public static boolean shouldPerformExpensiveCalculation() {
        switch (currentMode) {
            case FULL_SPEED:
                return true;
            case REDUCED:
                // 75% of calculations
                if (Math.random() > 0.75) {
                    calculationsReduced.incrementAndGet();
                    return false;
                }
                return true;
            case MINIMAL:
                // 50% of calculations
                if (Math.random() > 0.5) {
                    calculationsReduced.incrementAndGet();
                    return false;
                }
                return true;
            case BATTERY_SAVE:
                // 25% of calculations
                if (Math.random() > 0.25) {
                    calculationsReduced.incrementAndGet();
                    return false;
                }
                return true;
            default:
                return true;
        }
    }
    
    /**
     * Get detail level for building generation
     */
    public static int getBuildingDetailLevel() {
        switch (currentMode) {
            case FULL_SPEED:
                return 100; // Full detail
            case REDUCED:
                return 75;  // Reduced detail
            case MINIMAL:
                return 50;  // Minimal detail
            case BATTERY_SAVE:
                return 25;  // Ultra-low detail
            default:
                return 100;
        }
    }
    
    /**
     * Check if there are players near a chunk
     */
    private static boolean hasNearbyPlayers(ChunkPos pos, ServerLevel level, int chunkRadius) {
        int centerX = pos.x * 16 + 8;
        int centerZ = pos.z * 16 + 8;
        int radiusBlocks = chunkRadius * 16;
        
        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - centerX;
            double dz = player.getZ() - centerZ;
            double distSq = dx * dx + dz * dz;
            
            if (distSq < radiusBlocks * radiusBlocks) {
                lastPlayerActivity.set(System.currentTimeMillis());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Update performance mode based on server state
     */
    private static void updatePerformanceMode(ServerLevel level) {
        long now = System.currentTimeMillis();
        long timeSinceActivity = now - lastPlayerActivity.get();
        
        // Check server TPS
        double tps = getServerTPS(level);
        
        // Determine mode
        if (tps < CRITICAL_TPS_THRESHOLD) {
            // Server critically overloaded
            currentMode = PerformanceMode.BATTERY_SAVE;
            serverUnderLoad.set(true);
        } else if (tps < LOW_TPS_THRESHOLD) {
            // Server under moderate load
            currentMode = PerformanceMode.MINIMAL;
            serverUnderLoad.set(true);
        } else if (timeSinceActivity > IDLE_THRESHOLD_MS) {
            // Players idle/away
            currentMode = PerformanceMode.MINIMAL;
            serverUnderLoad.set(false);
        } else if (timeSinceActivity > CHUNK_IDLE_THRESHOLD_MS) {
            // Players not actively exploring
            currentMode = PerformanceMode.REDUCED;
            serverUnderLoad.set(false);
        } else {
            // Normal operation
            currentMode = PerformanceMode.FULL_SPEED;
            serverUnderLoad.set(false);
        }
    }
    
    /**
     * Get approximate server TPS
     */
    private static double getServerTPS(ServerLevel level) {
        try {
            // Get average tick time from server
            long[] tickTimes = level.getServer().tickTimes;
            if (tickTimes == null || tickTimes.length == 0) {
                return 20.0;
            }
            
            // Calculate average tick time
            long sum = 0;
            for (long tickTime : tickTimes) {
                sum += tickTime;
            }
            long avgTickTime = sum / tickTimes.length;
            
            // Convert to TPS (nanoseconds to TPS)
            double avgTickMs = avgTickTime / 1_000_000.0;
            double tps = 1000.0 / Math.max(avgTickMs, 50.0);
            
            return Math.min(tps, 20.0);
        } catch (Exception e) {
            // Fallback to 20 TPS if we can't read server state
            return 20.0;
        }
    }
    
    /**
     * Notify that a player performed an action
     */
    public static void notifyPlayerActivity() {
        lastPlayerActivity.set(System.currentTimeMillis());
    }
    
    /**
     * Check if server is under load
     */
    public static boolean isServerUnderLoad() {
        return serverUnderLoad.get();
    }
    
    /**
     * Get current performance mode
     */
    public static PerformanceMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Get performance statistics
     */
    public static String getStats() {
        long now = System.currentTimeMillis();
        long timeSinceActivity = now - lastPlayerActivity.get();
        
        return String.format(
            "Adaptive Performance - Mode: %s, Time Since Activity: %.1fs, Chunks Skipped: %d, Calculations Reduced: %d, Under Load: %s",
            currentMode,
            timeSinceActivity / 1000.0,
            chunksSkipped.get(),
            calculationsReduced.get(),
            serverUnderLoad.get()
        );
    }
    
    /**
     * Reset statistics
     */
    public static void resetStats() {
        chunksSkipped.set(0);
        calculationsReduced.set(0);
    }
    
    /**
     * Log current status
     */
    public static void logStatus() {
        LOGGER.info(getStats());
    }
}
