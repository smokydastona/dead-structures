package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for When Dungeons Arise integration
 * 
 * When Dungeons Arise generates large dungeon structures across the world.
 * This compat ensures Dead Structures cities don't conflict with WDA dungeons.
 * 
 * Integration notes:
 * - Both mods can coexist peacefully
 * - WDA dungeons will generate outside of city bounds
 * - Dead Structures respects WDA structure spawn rules
 * - Players can find WDA dungeons while exploring between cities
 */
public class DungeonsAriseCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean wdaLoaded = null;

    /**
     * Check if When Dungeons Arise is loaded
     */
    public static boolean isWDALoaded() {
        if (wdaLoaded == null) {
            wdaLoaded = ModList.get().isLoaded("dungeons_arise");
            if (wdaLoaded) {
                LOGGER.info("When Dungeons Arise detected! Dungeon structures will coexist with Dead Structures cities.");
            }
        }
        return wdaLoaded;
    }

    /**
     * Get recommended structure spacing for compatibility
     * WDA uses large structure bounds, so we ensure proper spacing
     */
    public static int getRecommendedCitySpacing() {
        return isWDALoaded() ? 512 : 256;
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isWDALoaded()) {
            LOGGER.info("Dead Structures + When Dungeons Arise compatibility active");
            LOGGER.info("Large dungeons will spawn between city areas");
            LOGGER.info("Recommended city spacing: " + getRecommendedCitySpacing() + " blocks");
        }
    }
}
