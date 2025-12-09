package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles placement of Apocalypse Overwrite structures in Lost Cities
 * Implements weighted template pool system for apocalyptic structures
 */
public class ApocalypseStructurePlacer {
    
    /**
     * Weighted pool entry for structure placement
     */
    private static class WeightedStructure {
        final String path;
        final int weight;
        
        WeightedStructure(String path, int weight) {
            this.path = path;
            this.weight = weight;
        }
    }
    
    // Structure pools based on datapack configuration
    private static final List<WeightedStructure> SKYSCRAPERS = new ArrayList<>();
    private static final List<WeightedStructure> HOUSES = new ArrayList<>();
    private static final List<WeightedStructure> COUNTRY_STRUCTURES = new ArrayList<>();
    private static final List<WeightedStructure> ROADSIDE_STRUCTURES = new ArrayList<>();
    private static final List<WeightedStructure> SPECIAL_STRUCTURES = new ArrayList<>();
    
    static {
        // Skyscrapers (for city areas)
        SKYSCRAPERS.add(new WeightedStructure("apocalypse/modern_apartment", 10));
        SKYSCRAPERS.add(new WeightedStructure("apocalypse/skyscraper_1", 10));
        SKYSCRAPERS.add(new WeightedStructure("apocalypse/skyscraper_2", 10));
        SKYSCRAPERS.add(new WeightedStructure("apocalypse/skyscraper_3", 10));
        SKYSCRAPERS.add(new WeightedStructure("apocalypse/skyscraper_4", 10));
        SKYSCRAPERS.add(new WeightedStructure("apocalypse/skyscraper_5", 10));
        SKYSCRAPERS.add(new WeightedStructure("apocalypse/skyscraper_6", 2));
        
        // Houses (for residential areas)
        HOUSES.add(new WeightedStructure("apocalypse/house_1", 1));
        HOUSES.add(new WeightedStructure("apocalypse/house_2", 1));
        HOUSES.add(new WeightedStructure("apocalypse/house_3", 1));
        HOUSES.add(new WeightedStructure("apocalypse/house_4", 1));
        HOUSES.add(new WeightedStructure("apocalypse/our_house", 1));
        HOUSES.add(new WeightedStructure("apocalypse/wilderness_house_1", 1));
        
        // Country/Rural structures
        COUNTRY_STRUCTURES.add(new WeightedStructure("apocalypse/forest_campsite_1", 1));
        COUNTRY_STRUCTURES.add(new WeightedStructure("apocalypse/plains_campsite_1", 1));
        COUNTRY_STRUCTURES.add(new WeightedStructure("apocalypse/blue_silo", 1));
        COUNTRY_STRUCTURES.add(new WeightedStructure("apocalypse/windmill1", 1));
        COUNTRY_STRUCTURES.add(new WeightedStructure("apocalypse/slum", 1));
        
        // Roadside structures
        ROADSIDE_STRUCTURES.add(new WeightedStructure("apocalypse/gas_station_1", 1));
        ROADSIDE_STRUCTURES.add(new WeightedStructure("apocalypse/gas_station_2", 1));
        
        // Special/Large structures
        SPECIAL_STRUCTURES.add(new WeightedStructure("apocalypse/hospital_1", 1));
        SPECIAL_STRUCTURES.add(new WeightedStructure("apocalypse/police_station", 1));
        SPECIAL_STRUCTURES.add(new WeightedStructure("apocalypse/supermarket", 1));
        SPECIAL_STRUCTURES.add(new WeightedStructure("apocalypse/bunker_center_1", 1));
        SPECIAL_STRUCTURES.add(new WeightedStructure("apocalypse/amusement_park", 1));
        SPECIAL_STRUCTURES.add(new WeightedStructure("apocalypse/passenger_plane", 1));
    }
    
    /**
     * Selects a weighted random structure from a pool
     */
    private static String selectFromPool(List<WeightedStructure> pool, RandomSource random) {
        if (pool.isEmpty()) return null;
        
        int totalWeight = pool.stream().mapToInt(ws -> ws.weight).sum();
        int selected = random.nextInt(totalWeight);
        
        int current = 0;
        for (WeightedStructure ws : pool) {
            current += ws.weight;
            if (selected < current) {
                return ws.path;
            }
        }
        
        return null;
    }
    
    /**
     * Places a random skyscraper in city areas
     */
    public static boolean placeSkyscraper(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        String structurePath = selectFromPool(SKYSCRAPERS, random);
        boolean placed = structurePath != null && placeStructure(level, structurePath, pos, random);
        if (placed && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            initializeHauntedBuilding(serverLevel, pos.getX() >> 4, pos.getZ() >> 4, random);
        }
        return placed;
    }
    
    /**
     * Places a random house
     */
    public static boolean placeHouse(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        String structurePath = selectFromPool(HOUSES, random);
        boolean placed = structurePath != null && placeStructure(level, structurePath, pos, random);
        if (placed && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            initializeHauntedBuilding(serverLevel, pos.getX() >> 4, pos.getZ() >> 4, random);
        }
        return placed;
    }
    
    /**
     * Places a random country/rural structure
     */
    public static boolean placeCountryStructure(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        String structurePath = selectFromPool(COUNTRY_STRUCTURES, random);
        boolean placed = structurePath != null && placeStructure(level, structurePath, pos, random);
        if (placed && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            initializeHauntedBuilding(serverLevel, pos.getX() >> 4, pos.getZ() >> 4, random);
        }
        return placed;
    }
    
    /**
     * Places a random roadside structure
     */
    public static boolean placeRoadsideStructure(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        String structurePath = selectFromPool(ROADSIDE_STRUCTURES, random);
        boolean placed = structurePath != null && placeStructure(level, structurePath, pos, random);
        if (placed && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            initializeHauntedBuilding(serverLevel, pos.getX() >> 4, pos.getZ() >> 4, random);
        }
        return placed;
    }
    
    /**
     * Places a random special/large structure
     */
    public static boolean placeSpecialStructure(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        String structurePath = selectFromPool(SPECIAL_STRUCTURES, random);
        boolean placed = structurePath != null && placeStructure(level, structurePath, pos, random);
        if (placed && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            initializeHauntedBuilding(serverLevel, pos.getX() >> 4, pos.getZ() >> 4, random);
        }
        return placed;
    }
    
    /**
     * Places a structure at the specified location
     */
    private static boolean placeStructure(ServerLevelAccessor level, String structurePath, 
                                         BlockPos pos, RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        
        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        ResourceLocation location = new ResourceLocation("lostcities", structurePath);
        StructureTemplate template = loadTemplate(templateManager, location);
        
        if (template == null) {
            return false;
        }
        
        Rotation rotation = Rotation.getRandom(random);
        Mirror mirror = random.nextBoolean() ? Mirror.NONE : Mirror.FRONT_BACK;
        
        // Validate position is above minimum world height
        int minY = serverLevel.getMinBuildHeight();
        BlockPos finalPos = pos;
        if (pos.getY() < minY) {
            finalPos = new BlockPos(pos.getX(), minY, pos.getZ());
        }
        
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(mirror)
                .setRandom(random)
                .setKeepLiquids(false);
        
        // Filter entities from template to prevent hanging entities below minimum height
        try {
            java.lang.reflect.Field entitiesField = StructureTemplate.class.getDeclaredField("f_74554_"); // entityInfoList
            entitiesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<StructureTemplate.StructureEntityInfo> originalEntities = 
                (java.util.List<StructureTemplate.StructureEntityInfo>) entitiesField.get(template);
            
            if (originalEntities != null && !originalEntities.isEmpty()) {
                final BlockPos placementPos = finalPos;
                final int minYFinal = minY;
                // Filter entities that would be placed below minimum world height
                java.util.List<StructureTemplate.StructureEntityInfo> filteredEntities = originalEntities.stream()
                    .filter(entity -> {
                        BlockPos entityWorldPos = StructureTemplate.calculateRelativePosition(settings, entity.blockPos).offset(placementPos);
                        return entityWorldPos.getY() >= minYFinal;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                // Temporarily replace entities with filtered list
                entitiesField.set(template, filteredEntities);
                template.placeInWorld(serverLevel, placementPos, placementPos, settings, random, 2);
                // Restore original entities
                entitiesField.set(template, originalEntities);
            } else {
                template.placeInWorld(serverLevel, finalPos, finalPos, settings, random, 2);
            }
        } catch (Exception e) {
            // If reflection fails, place normally (will still have hanging entity errors)
            LostCities.LOGGER.warn("Failed to filter entities, placing structure normally: " + e.getMessage());
            template.placeInWorld(serverLevel, finalPos, finalPos, settings, random, 2);
        }
        
        return true;
    }
    
    /**
     * Loads a structure template from NBT file
     */
    private static StructureTemplate loadTemplate(StructureTemplateManager manager, ResourceLocation location) {
        try {
            return manager.getOrCreate(location);
        } catch (Exception e) {
            LostCities.LOGGER.error("Failed to load apocalypse structure template: " + location, e);
            return null;
        }
    }
    
    /**
     * Initialize a haunted building system for placed structure
     */
    private static void initializeHauntedBuilding(net.minecraft.server.level.ServerLevel level, 
                                                   int chunkX, int chunkZ, RandomSource random) {
        HauntedBuildingHandler handler = HauntedBuildingHandler.get(level);
        handler.initializeHauntedBuilding(chunkX, chunkZ, random);
    }
    
    /**
     * Checks if a chunk should contain an apocalypse structure
     * More frequent than pillager outposts since these are smaller
     */
    public static boolean shouldGenerateStructure(ServerLevel level, int chunkX, int chunkZ, int frequency) {
        long seed = level.getSeed() + chunkX * 341873128712L + chunkZ * 132897987541L;
        RandomSource random = RandomSource.create(seed);
        return random.nextInt(frequency) == 0;
    }
}
