package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Cobweb mod integration
 * 
 * Cobweb adds decorative cobweb blocks that are perfect for
 * abandoned buildings in Dead Structures. Creates atmospheric
 * decay and abandonment effects.
 * 
 * Cobweb placement suggestions:
 * - Building corners and ceilings
 * - Dark basements and cellars
 * - Abandoned attics
 * - Broken windows and doorways
 * - Underground tunnels
 */
public class CobwebCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean cobwebLoaded = null;

    // Cobweb block ID
    public static final String COBWEB = "cobweb:cobweb";
    public static final String THICK_COBWEB = "cobweb:thick_cobweb";
    
    // Vanilla fallback
    private static final String FALLBACK_COBWEB = "minecraft:cobweb";

    /**
     * Check if Cobweb mod is loaded
     */
    public static boolean isCobwebLoaded() {
        if (cobwebLoaded == null) {
            cobwebLoaded = ModList.get().isLoaded("cobweb");
            if (cobwebLoaded) {
                LOGGER.info("Cobweb mod detected! Enhanced cobwebs will appear in abandoned buildings.");
            }
        }
        return cobwebLoaded;
    }

    /**
     * Get cobweb block with vanilla fallback
     */
    public static String getCobweb() {
        return isCobwebLoaded() ? COBWEB : FALLBACK_COBWEB;
    }

    /**
     * Get thick cobweb for heavily abandoned areas
     */
    public static String getThickCobweb() {
        return isCobwebLoaded() ? THICK_COBWEB : FALLBACK_COBWEB;
    }

    /**
     * Should spawn cobwebs in this location?
     * More cobwebs in dark, enclosed spaces
     */
    public static boolean shouldSpawnCobweb(int lightLevel, boolean isCorner) {
        if (!isCobwebLoaded()) {
            return false; // Don't spam vanilla cobwebs
        }
        
        // Higher chance in dark corners
        if (lightLevel < 5 && isCorner) {
            return Math.random() < 0.4;
        }
        
        // Lower chance in dim areas
        if (lightLevel < 10) {
            return Math.random() < 0.15;
        }
        
        return false;
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isCobwebLoaded()) {
            LOGGER.info("Dead Structures + Cobweb compatibility active");
            LOGGER.info("Atmospheric cobwebs will enhance abandoned building decay");
        }
    }
}
