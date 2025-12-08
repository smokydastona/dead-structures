package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Lootr integration
 * Lootr provides per-player loot chests in multiplayer
 * 
 * When Lootr is present, it will automatically convert vanilla loot chests
 * to per-player Lootr chests based on block tags and loot table detection.
 * 
 * No explicit API calls needed - Lootr handles conversion automatically!
 */
public class LootrCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean lootrLoaded = null;

    /**
     * Check if Lootr mod is loaded at runtime
     */
    public static boolean isLootrLoaded() {
        if (lootrLoaded == null) {
            lootrLoaded = ModList.get().isLoaded("lootr");
            if (lootrLoaded) {
                LOGGER.info("Lootr detected! Per-player loot chests will be available.");
            }
        }
        return lootrLoaded;
    }

    /**
     * Get recommended loot table prefix for Dead Structures
     * Lootr automatically detects and converts chests with loot tables
     */
    public static String getLootTableNamespace() {
        return "lostcities";
    }

    /**
     * Log Lootr integration status for debugging
     */
    public static void logStatus() {
        if (isLootrLoaded()) {
            LOGGER.info("Dead Structures + Lootr integration active");
            LOGGER.info("All loot chests will be converted to per-player Lootr chests");
            LOGGER.info("Loot tables: lostcities:chests/lostcitychest, lostcities:chests/raildungeonchest");
        } else {
            LOGGER.info("Lootr not detected - using vanilla loot chests");
        }
    }
}
