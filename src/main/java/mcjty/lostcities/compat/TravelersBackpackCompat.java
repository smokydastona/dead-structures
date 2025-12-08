package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Traveler's Backpack integration
 * 
 * Traveler's Backpack adds backpacks with inventory storage.
 * In Dead Structures, abandoned backpacks spawn in buildings
 * with randomized loot representing former inhabitants' belongings.
 * 
 * Backpack spawn locations:
 * - Building corners and hiding spots
 * - Rooftops (survivor camps)
 * - Underground bunkers
 * - Railway stations
 * - Hotels and apartments
 */
public class TravelersBackpackCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean tbLoaded = null;

    // Traveler's Backpack block/entity IDs
    public static final String STANDARD_BACKPACK = "travelersbackpack:standard";
    public static final String DIAMOND_BACKPACK = "travelersbackpack:diamond";
    public static final String GOLD_BACKPACK = "travelersbackpack:gold";
    public static final String EMERALD_BACKPACK = "travelersbackpack:emerald";
    public static final String IRON_BACKPACK = "travelersbackpack:iron";

    /**
     * Check if Traveler's Backpack is loaded
     */
    public static boolean isTravelersBackpackLoaded() {
        if (tbLoaded == null) {
            tbLoaded = ModList.get().isLoaded("travelersbackpack");
            if (tbLoaded) {
                LOGGER.info("Traveler's Backpack detected! Abandoned backpacks will spawn in Dead Structures.");
            }
        }
        return tbLoaded;
    }

    /**
     * Should spawn backpack in this location?
     */
    public static boolean shouldSpawnBackpack(String buildingType, boolean isHiddenCorner) {
        if (!isTravelersBackpackLoaded()) {
            return false;
        }
        
        // Higher chance in hidden spots
        if (isHiddenCorner) {
            return Math.random() < 0.15;
        }
        
        // Building-specific spawn rates
        if (buildingType != null) {
            if (buildingType.contains("hotel") || buildingType.contains("apartment")) {
                return Math.random() < 0.2;
            } else if (buildingType.contains("station") || buildingType.contains("railway")) {
                return Math.random() < 0.25;
            } else if (buildingType.contains("bunker") || buildingType.contains("shelter")) {
                return Math.random() < 0.3;
            }
        }
        
        return Math.random() < 0.05;
    }

    /**
     * Get backpack rarity based on location
     */
    public static String getBackpackType(int buildingTier, boolean isRooftop) {
        if (!isTravelersBackpackLoaded()) {
            return null;
        }
        
        double roll = Math.random();
        
        // Rooftop survivors had better gear
        if (isRooftop) {
            if (roll < 0.05) return DIAMOND_BACKPACK;
            if (roll < 0.15) return EMERALD_BACKPACK;
            if (roll < 0.35) return GOLD_BACKPACK;
            if (roll < 0.65) return IRON_BACKPACK;
            return STANDARD_BACKPACK;
        }
        
        // High-tier buildings have better loot
        if (buildingTier >= 3) {
            if (roll < 0.02) return DIAMOND_BACKPACK;
            if (roll < 0.08) return GOLD_BACKPACK;
            if (roll < 0.20) return IRON_BACKPACK;
        }
        
        // Standard distribution
        if (roll < 0.1) return IRON_BACKPACK;
        return STANDARD_BACKPACK;
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isTravelersBackpackLoaded()) {
            LOGGER.info("Dead Structures + Traveler's Backpack compatibility active");
            LOGGER.info("Abandoned backpacks with loot will spawn throughout buildings");
        }
    }
}
