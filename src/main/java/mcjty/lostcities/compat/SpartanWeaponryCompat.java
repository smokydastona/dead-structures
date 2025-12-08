package mcjty.lostcities.compat;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility layer for Spartan Weaponry integration
 * 
 * Spartan Weaponry adds diverse medieval and tactical weapons.
 * Perfect loot for Dead Structures armories, guard posts, and
 * military buildings.
 * 
 * Weapon spawn locations:
 * - Police stations and guard posts
 * - Military buildings
 * - Weapon shops
 * - Barracks
 * - Armory rooms
 */
public class SpartanWeaponryCompat {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Boolean swLoaded = null;

    /**
     * Check if Spartan Weaponry is loaded
     */
    public static boolean isSpartanWeaponryLoaded() {
        if (swLoaded == null) {
            swLoaded = ModList.get().isLoaded("spartanweaponry");
            if (swLoaded) {
                LOGGER.info("Spartan Weaponry detected! Weapon loot will spawn in military buildings.");
            }
        }
        return swLoaded;
    }

    /**
     * Should spawn weapons in this building?
     */
    public static boolean shouldSpawnWeapons(String buildingType) {
        if (!isSpartanWeaponryLoaded()) {
            return false;
        }
        
        if (buildingType != null) {
            if (buildingType.contains("police") || buildingType.contains("guard")) {
                return Math.random() < 0.7;
            } else if (buildingType.contains("military") || buildingType.contains("armory")) {
                return Math.random() < 0.9;
            } else if (buildingType.contains("weapon") || buildingType.contains("shop")) {
                return Math.random() < 0.5;
            }
        }
        
        return Math.random() < 0.05;
    }

    /**
     * Get weapon quality based on building tier
     */
    public static String getWeaponQuality(int buildingTier) {
        if (!isSpartanWeaponryLoaded()) {
            return "iron";
        }
        
        double roll = Math.random();
        
        if (buildingTier >= 4) {
            // High-tier military buildings
            if (roll < 0.1) return "diamond";
            if (roll < 0.3) return "gold";
            if (roll < 0.6) return "steel";
            return "iron";
        } else if (buildingTier >= 2) {
            // Standard security
            if (roll < 0.05) return "diamond";
            if (roll < 0.2) return "steel";
            if (roll < 0.5) return "iron";
            return "bronze";
        } else {
            // Low-tier
            if (roll < 0.3) return "iron";
            if (roll < 0.6) return "bronze";
            return "wood";
        }
    }

    /**
     * Log compatibility status
     */
    public static void logStatus() {
        if (isSpartanWeaponryLoaded()) {
            LOGGER.info("Dead Structures + Spartan Weaponry compatibility active");
            LOGGER.info("Diverse weapon loot will spawn in military and security buildings");
        }
    }
}
