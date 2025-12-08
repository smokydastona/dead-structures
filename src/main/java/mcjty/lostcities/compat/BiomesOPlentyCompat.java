package mcjty.lostcities.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

/**
 * Soft dependency integration for Biomes O' Plenty
 * This class allows Lost Cities to use BOP blocks when available
 * without requiring BOP as a hard dependency
 */
public class BiomesOPlentyCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BOP_MODID = "biomesoplenty";
    
    private static Boolean bopLoaded = null;
    
    /**
     * Check if Biomes O' Plenty is loaded
     */
    public static boolean isBOPLoaded() {
        if (bopLoaded == null) {
            // Check if any BOP block exists in the registry
            bopLoaded = BuiltInRegistries.BLOCK.containsKey(new ResourceLocation(BOP_MODID, "white_cherry_planks"));
            if (bopLoaded) {
                LOGGER.info("Biomes O' Plenty detected - enabling integration features");
            } else {
                LOGGER.debug("Biomes O' Plenty not found - using vanilla fallbacks");
            }
        }
        return bopLoaded;
    }
    
    /**
     * Get a BOP block if available, otherwise return fallback
     * @param bopBlockName The BOP block name (without modid)
     * @param fallback The vanilla fallback block
     * @return The BOP block if available, otherwise fallback
     */
    public static Block getBlockOrFallback(String bopBlockName, Block fallback) {
        if (!isBOPLoaded()) {
            return fallback;
        }
        
        ResourceLocation bopLoc = new ResourceLocation(BOP_MODID, bopBlockName);
        if (BuiltInRegistries.BLOCK.containsKey(bopLoc)) {
            return BuiltInRegistries.BLOCK.get(bopLoc);
        }
        
        LOGGER.warn("BOP block {} not found, using fallback", bopBlockName);
        return fallback;
    }
    
    /**
     * Get a BOP block resource location string for use in palette JSON files
     * @param bopBlockName The BOP block name
     * @param properties Block properties (e.g., "[axis=y]")
     * @param fallbackBlock The vanilla fallback block string
     * @return The block string to use in palette JSON
     */
    public static String getBlockString(String bopBlockName, @Nullable String properties, String fallbackBlock) {
        String bopBlock = BOP_MODID + ":" + bopBlockName;
        if (properties != null && !properties.isEmpty()) {
            bopBlock += properties;
        }
        
        // This will be used in JSON files with conditional loading
        // The palette system should handle missing blocks gracefully
        return bopBlock;
    }
    
    // Common BOP wood types
    public static final String CHERRY_PLANKS = "white_cherry_planks";
    public static final String CHERRY_LOG = "white_cherry_log";
    public static final String CHERRY_STAIRS = "white_cherry_stairs";
    public static final String CHERRY_SLAB = "white_cherry_slab";
    
    public static final String DEAD_PLANKS = "dead_planks";
    public static final String DEAD_LOG = "dead_log";
    public static final String DEAD_STAIRS = "dead_stairs";
    public static final String DEAD_SLAB = "dead_slab";
    
    public static final String FIR_PLANKS = "fir_planks";
    public static final String FIR_LOG = "fir_log";
    public static final String FIR_STAIRS = "fir_stairs";
    public static final String FIR_SLAB = "fir_slab";
    
    public static final String WILLOW_PLANKS = "willow_planks";
    public static final String WILLOW_LOG = "willow_log";
    public static final String WILLOW_STAIRS = "willow_stairs";
    public static final String WILLOW_SLAB = "willow_slab";
    
    public static final String MAGIC_PLANKS = "magic_planks";
    public static final String MAGIC_LOG = "magic_log";
    public static final String MAGIC_STAIRS = "magic_stairs";
    public static final String MAGIC_SLAB = "magic_slab";
    
    public static final String MAHOGANY_PLANKS = "mahogany_planks";
    public static final String MAHOGANY_LOG = "mahogany_log";
    public static final String MAHOGANY_STAIRS = "mahogany_stairs";
    public static final String MAHOGANY_SLAB = "mahogany_slab";
    
    public static final String REDWOOD_PLANKS = "redwood_planks";
    public static final String REDWOOD_LOG = "redwood_log";
    public static final String REDWOOD_STAIRS = "redwood_stairs";
    public static final String REDWOOD_SLAB = "redwood_slab";
    
    public static final String UMBRAN_PLANKS = "umbran_planks";
    public static final String UMBRAN_LOG = "umbran_log";
    public static final String UMBRAN_STAIRS = "umbran_stairs";
    public static final String UMBRAN_SLAB = "umbran_slab";
    
    public static final String HELLBARK_PLANKS = "hellbark_planks";
    public static final String HELLBARK_LOG = "hellbark_log";
    public static final String HELLBARK_STAIRS = "hellbark_stairs";
    public static final String HELLBARK_SLAB = "hellbark_slab";
    
    public static final String JACARANDA_PLANKS = "jacaranda_planks";
    public static final String JACARANDA_LOG = "jacaranda_log";
    public static final String JACARANDA_STAIRS = "jacaranda_stairs";
    public static final String JACARANDA_SLAB = "jacaranda_slab";
    
    // Common BOP decorative blocks
    public static final String WHITE_SAND = "white_sand";
    public static final String WHITE_SANDSTONE = "white_sandstone";
    public static final String ORANGE_SANDSTONE = "orange_sandstone";
    public static final String BLACK_SANDSTONE = "black_sandstone";
    
    public static final String MUD_BRICKS = "mud_bricks";
    public static final String MUD_BRICK_STAIRS = "mud_brick_stairs";
    public static final String MUD_BRICK_SLAB = "mud_brick_slab";
    
    // Common BOP plants (for decoration)
    public static final String WILLOW_VINE = "willow_vine";
    public static final String SPANISH_MOSS = "spanish_moss";
    public static final String REED = "reed";
    public static final String TOADSTOOL = "toadstool";
    public static final String GLOWSHROOM = "glowshroom";
}
