package mcjty.lostcities.varia;

import mcjty.lostcities.LostCities;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;

/**
 * Filters annoying log messages from Lost Cities and other mods
 * Based on lcchatlogfilter by community
 */
public class LogFilter extends AbstractFilter {
    
    // Messages to filter out
    private static final String[] FILTERED_MESSAGES = {
        "Structure piece",
        "Failed to find structure piece",
        "Could not find structure piece",
        "Unrecognized structure piece",
        "Missing structure piece",
        "Structure start",
        "Invalid structure",
        "Skipping structure",
        "Unable to find spawn egg for",
        "Unknown recipe serializer",
        "Unknown advancement criterion trigger",
        "Recipe",
        "Ignored advancement",
        "Found a duplicate recipe",
        "Duplicate recipe",
        "Missing registry entry",
        "Registry entry",
        "Biome entry"
    };
    
    private static final String[] FILTERED_CLASSES = {
        "net.minecraft.world.level.chunk.ChunkGenerator",
        "net.minecraft.world.level.levelgen.structure",
        "net.minecraft.world.item.crafting",
        "net.minecraft.advancements",
        "net.minecraft.core.Registry",
        "net.minecraftforge.registries"
    };
    
    public LogFilter() {
        super();
    }
    
    @Override
    public Result filter(LogEvent event) {
        if (event == null) {
            return Result.NEUTRAL;
        }
        
        String message = event.getMessage().getFormattedMessage();
        String loggerName = event.getLoggerName();
        
        // Filter by message content
        for (String filtered : FILTERED_MESSAGES) {
            if (message.contains(filtered)) {
                return Result.DENY;
            }
        }
        
        // Filter by logger class
        for (String filtered : FILTERED_CLASSES) {
            if (loggerName != null && loggerName.startsWith(filtered)) {
                // Only filter WARN and INFO, keep ERROR
                if (event.getLevel().intLevel() > Level.ERROR.intLevel()) {
                    return Result.DENY;
                }
            }
        }
        
        return Result.NEUTRAL;
    }
    
    /**
     * Registers the log filter
     */
    public static void register() {
        try {
            Logger rootLogger = (Logger) LogManager.getRootLogger();
            rootLogger.addFilter(new LogFilter());
            LostCities.LOGGER.info("Log filter registered successfully");
        } catch (Exception e) {
            LostCities.LOGGER.error("Failed to register log filter", e);
        }
    }
}
