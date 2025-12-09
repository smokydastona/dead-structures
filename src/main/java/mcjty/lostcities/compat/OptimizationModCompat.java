package mcjty.lostcities.compat;

import mcjty.lostcities.varia.PerformanceOptimizer;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for performance optimization mods
 * Detects and adapts to:
 * - ModernFix: Launch times, world load times, memory usage
 * - FerriteCore: Memory reduction
 * - Starlight: Lighting engine
 * - Canary/Radium: General optimizations (Lithium port)
 * - AI Improvements: Mob AI optimizations
 * - MemoryLeakFix: Memory leak fixes
 * - Clumps: XP orb optimization
 * - Get It Together Drops: Item drop optimization
 */
public class OptimizationModCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static boolean modernFixLoaded = false;
    private static boolean ferriteCoreLoaded = false;
    private static boolean starlightLoaded = false;
    private static boolean canaryLoaded = false;
    private static boolean radiumLoaded = false;
    private static boolean aiImprovementsLoaded = false;
    private static boolean memoryLeakFixLoaded = false;
    
    /**
     * Initialize compatibility checks
     */
    public static void init() {
        modernFixLoaded = ModList.get().isLoaded("modernfix");
        ferriteCoreLoaded = ModList.get().isLoaded("ferritecore");
        starlightLoaded = ModList.get().isLoaded("starlight");
        canaryLoaded = ModList.get().isLoaded("canary");
        radiumLoaded = ModList.get().isLoaded("radium");
        aiImprovementsLoaded = ModList.get().isLoaded("aiimprovements");
        memoryLeakFixLoaded = ModList.get().isLoaded("memoryleakfix");
        
        logCompatibilityStatus();
        applyOptimizationSettings();
    }
    
    /**
     * Log detected optimization mods
     */
    private static void logCompatibilityStatus() {
        LOGGER.info("=== Optimization Mod Compatibility ===");
        if (modernFixLoaded) {
            LOGGER.info("✓ ModernFix detected - Launch and memory optimizations active");
        }
        if (ferriteCoreLoaded) {
            LOGGER.info("✓ FerriteCore detected - Memory reduction active");
        }
        if (starlightLoaded) {
            LOGGER.info("✓ Starlight detected - Enhanced lighting engine active");
        }
        if (canaryLoaded || radiumLoaded) {
            LOGGER.info("✓ {" + (canaryLoaded ? "Canary" : "Radium") + 
                       "} detected - General optimizations active");
        }
        if (aiImprovementsLoaded) {
            LOGGER.info("✓ AI Improvements detected - Mob AI optimizations active");
        }
        if (memoryLeakFixLoaded) {
            LOGGER.info("✓ MemoryLeakFix detected - Memory leak prevention active");
        }
        
        if (!modernFixLoaded && !ferriteCoreLoaded && !starlightLoaded && 
            !canaryLoaded && !radiumLoaded) {
            LOGGER.warn("⚠ No optimization mods detected - Consider adding ModernFix and FerriteCore");
        }
        LOGGER.info("=====================================");
    }
    
    /**
     * Apply optimization settings based on detected mods
     */
    private static void applyOptimizationSettings() {
        // If ModernFix or FerriteCore is present, enable performance mode
        if (modernFixLoaded || ferriteCoreLoaded) {
            PerformanceOptimizer.enablePerformanceMode();
            LOGGER.info("Enabled aggressive performance mode due to detected optimization mods");
        }
        
        // Additional compatibility settings can be added here
    }
    
    // Getters for other mods to check compatibility
    public static boolean isModernFixLoaded() {
        return modernFixLoaded;
    }
    
    public static boolean isStarlightLoaded() {
        return starlightLoaded;
    }
    
    public static boolean hasMemoryOptimizations() {
        return modernFixLoaded || ferriteCoreLoaded || memoryLeakFixLoaded;
    }
    
    public static boolean hasAIOptimizations() {
        return aiImprovementsLoaded || canaryLoaded || radiumLoaded;
    }
    
    public static boolean hasLightingOptimizations() {
        return starlightLoaded;
    }
}
