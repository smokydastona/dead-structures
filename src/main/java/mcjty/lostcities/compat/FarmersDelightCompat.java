package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Farmer's Delight integration
 * 
 * Farmer's Delight adds cooking and farming content that fits perfectly
 * in Dead Structures kitchens, restaurants, and abandoned farms.
 * 
 * FD blocks useful in Dead Structures:
 * - Stoves and cooking pots (restaurant kitchens)
 * - Cabinets and baskets (storage areas)
 * - Pantry items (abandoned food stores)
 * - Tomato/onion crates (market areas)
 */
public class FarmersDelightCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean fdLoaded = null;

    // Farmer's Delight block IDs
    public static final String STOVE = "farmersdelight:stove";
    public static final String COOKING_POT = "farmersdelight:cooking_pot";
    public static final String BASKET = "farmersdelight:basket";
    public static final String CABINET = "farmersdelight:oak_cabinet";
    public static final String PANTRY = "farmersdelight:oak_pantry";
    public static final String CUTTING_BOARD = "farmersdelight:cutting_board";
    public static final String TOMATO_CRATE = "farmersdelight:tomato_crate";
    public static final String ONION_CRATE = "farmersdelight:onion_crate";

    // Vanilla fallbacks
    private static final String FALLBACK_STOVE = "minecraft:furnace";
    private static final String FALLBACK_POT = "minecraft:cauldron";
    private static final String FALLBACK_STORAGE = "minecraft:barrel";
    private static final String FALLBACK_CRATE = "minecraft:composter";

    /**
     * Check if Farmer's Delight is loaded
     */
    public static boolean isFDLoaded() {
        if (fdLoaded == null) {
            fdLoaded = ModList.get().isLoaded("farmersdelight");
            if (fdLoaded) {
                LOGGER.info("Farmer's Delight detected! Kitchen blocks available for Dead Structures.");
            }
        }
        return fdLoaded;
    }

    /**
     * Get block with vanilla fallback
     */
    public static String getBlockOrFallback(String fdBlock, String vanillaFallback) {
        return isFDLoaded() ? fdBlock : vanillaFallback;
    }

    /**
     * Get cooking stove (furnace if FD not loaded)
     */
    public static String getStove() {
        return getBlockOrFallback(STOVE, FALLBACK_STOVE);
    }

    /**
     * Get cooking pot (cauldron if FD not loaded)
     */
    public static String getCookingPot() {
        return getBlockOrFallback(COOKING_POT, FALLBACK_POT);
    }

    /**
     * Get cabinet (barrel if FD not loaded)
     */
    public static String getCabinet() {
        return getBlockOrFallback(CABINET, FALLBACK_STORAGE);
    }

    /**
     * Get crate (composter if FD not loaded)
     */
    public static String getCrate() {
        return getBlockOrFallback(TOMATO_CRATE, FALLBACK_CRATE);
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isFDLoaded()) {
            LOGGER.info("Dead Structures + Farmer's Delight compatibility active");
            LOGGER.info("Kitchen and food storage blocks will populate restaurants and homes");
        } else {
            LOGGER.info("Farmer's Delight not detected - using vanilla block alternatives");
        }
    }
}
