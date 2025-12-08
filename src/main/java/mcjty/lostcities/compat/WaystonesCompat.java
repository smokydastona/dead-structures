package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Waystones integration
 * 
 * Waystones adds teleportation waypoints across the world.
 * Integration allows spawning waystones in Dead Structures city centers
 * for fast travel between abandoned cities.
 * 
 * Waystone placement suggestions:
 * - City center plazas (major cities)
 * - Highway rest stops (between cities)
 * - Railway stations (near rail dungeons)
 * - Building rooftops (tall towers)
 */
public class WaystonesCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean wsLoaded = null;

    // Waystone block IDs
    public static final String WAYSTONE = "waystones:waystone";
    public static final String MOSSY_WAYSTONE = "waystones:mossy_waystone";
    public static final String SANDY_WAYSTONE = "waystones:sandy_waystone";

    /**
     * Check if Waystones is loaded
     */
    public static boolean isWaystonesLoaded() {
        if (wsLoaded == null) {
            wsLoaded = ModList.get().isLoaded("waystones");
            if (wsLoaded) {
                LOGGER.info("Waystones detected! Fast travel points can spawn in Dead Structures cities.");
            }
        }
        return wsLoaded;
    }

    /**
     * Get waystone type based on biome/city style
     */
    public static String getWaystoneType(String cityStyle) {
        if (!isWaystonesLoaded()) {
            return "minecraft:stone_bricks";
        }

        // Match waystone type to city aesthetic
        if (cityStyle != null) {
            if (cityStyle.contains("desert")) {
                return SANDY_WAYSTONE;
            } else if (cityStyle.contains("overgrown") || cityStyle.contains("ruins")) {
                return MOSSY_WAYSTONE;
            }
        }
        return WAYSTONE;
    }

    /**
     * Should spawn waystone in this city?
     * Only spawn in major cities to avoid oversaturation
     */
    public static boolean shouldSpawnWaystone(int cityRadius) {
        return isWaystonesLoaded() && cityRadius > 100;
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isWaystonesLoaded()) {
            LOGGER.info("Dead Structures + Waystones compatibility active");
            LOGGER.info("Waystones will spawn in major city centers for fast travel");
        }
    }
}
