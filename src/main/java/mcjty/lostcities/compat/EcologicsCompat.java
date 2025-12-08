package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Ecologics integration
 * 
 * Ecologics adds natural environmental features and wildlife.
 * Perfect for showing nature reclaiming abandoned cities.
 * 
 * Integration features:
 * - Coconut trees growing through buildings
 * - Flowering azalea bushes in parks
 * - Moss carpets on stone surfaces
 * - Wildlife nests in ruins
 * - Natural overgrowth through cracks
 */
public class EcologicsCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean ecoLoaded = null;

    // Ecologics block IDs
    public static final String COCONUT_SEEDLING = "ecologics:coconut_seedling";
    public static final String AZALEA_FLOWER = "ecologics:azalea_flower";
    public static final String MOSS_CARPET = "ecologics:thin_moss";
    public static final String PRICKLY_PEAR = "ecologics:prickly_pear";

    /**
     * Check if Ecologics is loaded
     */
    public static boolean isEcologicsLoaded() {
        if (ecoLoaded == null) {
            ecoLoaded = ModList.get().isLoaded("ecologics");
            if (ecoLoaded) {
                LOGGER.info("Ecologics detected! Natural overgrowth will reclaim Dead Structures.");
            }
        }
        return ecoLoaded;
    }

    /**
     * Should spawn overgrowth in this location?
     */
    public static boolean shouldSpawnOvergrowth(boolean isExterior, int timeSinceAbandonment) {
        if (!isEcologicsLoaded()) {
            return false;
        }
        
        // More overgrowth outside and in older ruins
        if (isExterior) {
            return Math.random() < (0.3 + timeSinceAbandonment * 0.1);
        }
        
        return Math.random() < (0.1 + timeSinceAbandonment * 0.05);
    }

    /**
     * Get vegetation type based on biome
     */
    public static String getVegetationType(String biome) {
        if (!isEcologicsLoaded()) {
            return "minecraft:grass";
        }
        
        if (biome != null) {
            if (biome.contains("desert")) {
                return PRICKLY_PEAR;
            } else if (biome.contains("jungle") || biome.contains("tropical")) {
                return COCONUT_SEEDLING;
            } else if (biome.contains("forest")) {
                return AZALEA_FLOWER;
            }
        }
        
        return MOSS_CARPET;
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isEcologicsLoaded()) {
            LOGGER.info("Dead Structures + Ecologics compatibility active");
            LOGGER.info("Nature will reclaim abandoned cities with diverse flora");
        }
    }
}
