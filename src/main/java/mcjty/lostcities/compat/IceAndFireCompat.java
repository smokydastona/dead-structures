package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Ice and Fire: Dragons integration
 * 
 * Ice and Fire adds dragons, dragon lairs, and mythical creatures.
 * Integration allows dragon-destroyed city ruins and dragon roosts
 * on tall buildings.
 * 
 * Integration features:
 * - Dragon roosts on skyscrapers
 * - Fire/ice damage to building exteriors
 * - Dragon skeleton decorations in ruins
 * - Mythical creature spawns in abandoned areas
 * - Dragon lairs beneath cities
 */
public class IceAndFireCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean iafLoaded = null;

    // Ice and Fire block IDs
    public static final String CHARRED_STONE = "iceandfire:chared_stone";
    public static final String CHARRED_COBBLESTONE = "iceandfire:chared_cobblestone";
    public static final String FROZEN_STONE = "iceandfire:frozen_stone";
    public static final String DRAGON_BONE_BLOCK = "iceandfire:dragon_bone_block";
    public static final String DRAGON_SCALE_RED = "iceandfire:dragon_scale_red";
    public static final String DRAGON_SCALE_ICE = "iceandfire:dragon_scale_ice";

    /**
     * Check if Ice and Fire is loaded
     */
    public static boolean isIceAndFireLoaded() {
        if (iafLoaded == null) {
            iafLoaded = ModList.get().isLoaded("iceandfire");
            if (iafLoaded) {
                LOGGER.info("Ice and Fire detected! Dragon-themed ruins and roosts will enhance Dead Structures.");
            }
        }
        return iafLoaded;
    }

    /**
     * Get fire-damaged block variant
     */
    public static String getCharredBlock(String originalBlock) {
        if (!isIceAndFireLoaded()) {
            return originalBlock;
        }
        
        if (originalBlock.contains("stone_bricks")) {
            return CHARRED_STONE;
        } else if (originalBlock.contains("cobblestone")) {
            return CHARRED_COBBLESTONE;
        }
        
        return originalBlock;
    }

    /**
     * Get ice-damaged block variant
     */
    public static String getFrozenBlock(String originalBlock) {
        if (!isIceAndFireLoaded()) {
            return originalBlock;
        }
        
        if (originalBlock.contains("stone")) {
            return FROZEN_STONE;
        }
        
        return originalBlock;
    }

    /**
     * Should spawn dragon roost on this building?
     * Only on very tall buildings
     */
    public static boolean shouldSpawnDragonRoost(int buildingHeight) {
        return isIceAndFireLoaded() && buildingHeight > 50 && Math.random() < 0.05;
    }

    /**
     * Should apply fire damage to building exterior?
     */
    public static boolean shouldApplyFireDamage() {
        return isIceAndFireLoaded() && Math.random() < 0.1;
    }

    /**
     * Should apply ice damage to building exterior?
     */
    public static boolean shouldApplyIceDamage() {
        return isIceAndFireLoaded() && Math.random() < 0.1;
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isIceAndFireLoaded()) {
            LOGGER.info("Dead Structures + Ice and Fire compatibility active");
            LOGGER.info("Dragon roosts, fire/ice damage, and mythical ruins will appear");
        }
    }
}
