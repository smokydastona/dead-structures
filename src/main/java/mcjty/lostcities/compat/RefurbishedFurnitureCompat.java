package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Refurbished Furniture integration
 * 
 * Refurbished Furniture adds decorative furniture blocks perfect for
 * abandoned city interiors. This compat enables using RF furniture
 * in Dead Structures buildings.
 * 
 * Furniture blocks that work well in Dead Structures:
 * - Chairs, tables, desks (offices, homes)
 * - Storage furniture (abandoned loot areas)
 * - Kitchen appliances (residential buildings)
 * - Bathroom fixtures (hotels, apartments)
 * - Lighting fixtures (decorative atmosphere)
 */
public class RefurbishedFurnitureCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean rfLoaded = null;

    // Refurbished Furniture block IDs (1.20.1)
    public static final String LIGHT_OAK_CHAIR = "refurbished_furniture:light_oak_chair";
    public static final String LIGHT_OAK_TABLE = "refurbished_furniture:light_oak_table";
    public static final String LIGHT_OAK_DESK = "refurbished_furniture:light_oak_desk";
    public static final String STORAGE_CABINET = "refurbished_furniture:storage_cabinet";
    public static final String KITCHEN_DRAWER = "refurbished_furniture:kitchen_drawer";
    public static final String KITCHEN_SINK = "refurbished_furniture:kitchen_sink";
    public static final String TOILET = "refurbished_furniture:toilet";
    public static final String BATH = "refurbished_furniture:bath";
    public static final String LIGHT_CEILING_LIGHT = "refurbished_furniture:light_ceiling_light";
    public static final String LIGHT_STANDING_LAMP = "refurbished_furniture:light_standing_lamp";

    // Fallback vanilla blocks
    private static final String FALLBACK_CHAIR = "minecraft:oak_stairs";
    private static final String FALLBACK_TABLE = "minecraft:oak_fence";
    private static final String FALLBACK_STORAGE = "minecraft:barrel";
    private static final String FALLBACK_LIGHT = "minecraft:lantern";

    /**
     * Check if Refurbished Furniture is loaded
     */
    public static boolean isRFLoaded() {
        if (rfLoaded == null) {
            rfLoaded = ModList.get().isLoaded("refurbished_furniture");
            if (rfLoaded) {
                LOGGER.info("Refurbished Furniture detected! Furniture blocks available for Dead Structures interiors.");
            }
        }
        return rfLoaded;
    }

    /**
     * Get furniture block with vanilla fallback
     */
    public static String getBlockOrFallback(String furnitureBlock, String vanillaFallback) {
        return isRFLoaded() ? furnitureBlock : vanillaFallback;
    }

    /**
     * Get chair block (stairs if RF not loaded)
     */
    public static String getChair() {
        return getBlockOrFallback(LIGHT_OAK_CHAIR, FALLBACK_CHAIR);
    }

    /**
     * Get table block (fence if RF not loaded)
     */
    public static String getTable() {
        return getBlockOrFallback(LIGHT_OAK_TABLE, FALLBACK_TABLE);
    }

    /**
     * Get storage block (barrel if RF not loaded)
     */
    public static String getStorage() {
        return getBlockOrFallback(STORAGE_CABINET, FALLBACK_STORAGE);
    }

    /**
     * Get light fixture (lantern if RF not loaded)
     */
    public static String getLight() {
        return getBlockOrFallback(LIGHT_CEILING_LIGHT, FALLBACK_LIGHT);
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isRFLoaded()) {
            LOGGER.info("Dead Structures + Refurbished Furniture compatibility active");
            LOGGER.info("Furniture blocks will enhance building interiors");
        } else {
            LOGGER.info("Refurbished Furniture not detected - using vanilla block alternatives");
        }
    }
}
