package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Minecraft Comes Alive integration
 * 
 * MCA adds villagers with personalities and relationships.
 * In Dead Structures, zombie villagers spawn in abandoned buildings,
 * representing the former inhabitants.
 * 
 * Integration features:
 * - Zombie villagers in residential buildings
 * - Villager skeletons/remains as decorations
 * - Personal belongings in homes
 * - Family photos and items
 * - Abandoned villages within cities
 */
public class MinecraftComesAliveCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean mcaLoaded = null;

    /**
     * Check if Minecraft Comes Alive is loaded
     */
    public static boolean isMCALoaded() {
        if (mcaLoaded == null) {
            mcaLoaded = ModList.get().isLoaded("mca");
            if (mcaLoaded) {
                LOGGER.info("Minecraft Comes Alive detected! Zombie villagers will inhabit abandoned buildings.");
            }
        }
        return mcaLoaded;
    }

    /**
     * Should spawn zombie villagers in this building?
     * More common in residential areas
     */
    public static boolean shouldSpawnZombieVillagers(String buildingType) {
        if (!isMCALoaded()) {
            return false;
        }
        
        if (buildingType != null) {
            if (buildingType.contains("residential") || buildingType.contains("apartment")) {
                return Math.random() < 0.3;
            } else if (buildingType.contains("hospital") || buildingType.contains("school")) {
                return Math.random() < 0.2;
            }
        }
        
        return Math.random() < 0.1;
    }

    /**
     * Get zombie villager spawn count for building size
     */
    public static int getZombieVillagerCount(int buildingVolume) {
        if (!isMCALoaded()) {
            return 0;
        }
        
        // Scale with building size
        int baseCount = buildingVolume / 1000;
        return Math.min(Math.max(1, baseCount), 5); // 1-5 zombies
    }

    /**
     * Should add personal belongings to this room?
     */
    public static boolean shouldAddPersonalItems() {
        return isMCALoaded() && Math.random() < 0.25;
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isMCALoaded()) {
            LOGGER.info("Dead Structures + Minecraft Comes Alive compatibility active");
            LOGGER.info("Zombie villagers will populate abandoned residential buildings");
        }
    }
}
