package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles placement of Pillager Outpost structures in Lost Cities
 * Implements weighted template pool system matching the datapack configuration
 */
public class PillagerOutpostStructurePlacer {
    
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
    
    // Template pools matching datapack configuration
    private static final List<WeightedStructure> BASE_PLATES = new ArrayList<>();
    private static final List<WeightedStructure> SPREAD_PLATES = new ArrayList<>();
    private static final List<WeightedStructure> FEATURES = new ArrayList<>();
    
    static {
        // Base plates pool - only one entry in datapack
        BASE_PLATES.add(new WeightedStructure("pillager_outpost/pillager_centers/base_plate", 1));
        
        // Spread plates pool - weighted as per datapack
        SPREAD_PLATES.add(new WeightedStructure("pillager_outpost/spread_plate_crossroad", 3));
        SPREAD_PLATES.add(new WeightedStructure("pillager_outpost/spread_plate_curve", 2));
        SPREAD_PLATES.add(new WeightedStructure("pillager_outpost/spread_plate_t_crossing", 1));
        SPREAD_PLATES.add(new WeightedStructure(null, 2)); // Empty with weight 2
        
        // Features pool - all weight 1, empty weight 8
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_cage1", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_cage2", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_cage3", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_cage_with_allays", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_logs", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_tent1", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_tent2", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_targets", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_tent3", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_tent4", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_fire", 1));
        FEATURES.add(new WeightedStructure("pillager_outpost/feature_backhoe", 1));
        FEATURES.add(new WeightedStructure(null, 8)); // Empty with weight 8 (~40% chance)
    }
    
    /**
     * Selects a weighted random structure from a pool
     */
    private static String selectFromPool(List<WeightedStructure> pool, RandomSource random) {
        int totalWeight = pool.stream().mapToInt(ws -> ws.weight).sum();
        int selected = random.nextInt(totalWeight);
        
        int current = 0;
        for (WeightedStructure ws : pool) {
            current += ws.weight;
            if (selected < current) {
                return ws.path; // May be null for empty entries
            }
        }
        
        return null;
    }
    
    /**
     * Places a complete Pillager Outpost structure with features
     * Matches datapack structure generation with weighted template pools
     */
    public static boolean placeOutpost(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        
        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        
        // Phase 1: Place base plate (main tower)
        String basePlatePath = selectFromPool(BASE_PLATES, random);
        if (basePlatePath == null) return false;
        
        Rotation baseRotation = Rotation.getRandom(random);
        if (!placeStructure(serverLevel, templateManager, basePlatePath, pos, baseRotation, random)) {
            return false;
        }
        
        // Phase 2: Place spread plates (pathways) in a radius around base
        // Datapack uses max_distance_from_center: 80, size: 7
        int spreadPlateCount = 3 + random.nextInt(4); // 3-6 spread plates
        for (int i = 0; i < spreadPlateCount; i++) {
            String spreadPlatePath = selectFromPool(SPREAD_PLATES, random);
            if (spreadPlatePath != null) {
                // Place at varying distances (20-60 blocks from center)
                int distance = 20 + random.nextInt(40);
                double angle = random.nextDouble() * Math.PI * 2;
                int offsetX = (int)(Math.cos(angle) * distance);
                int offsetZ = (int)(Math.sin(angle) * distance);
                
                BlockPos platePos = pos.offset(offsetX, 0, offsetZ);
                placeStructure(serverLevel, templateManager, spreadPlatePath, platePos, 
                             Rotation.getRandom(random), random);
            }
        }
        
        // Phase 3: Place features in a wider radius (matching datapack's max_distance: 80)
        // Total features: about 8-15 attempts with ~40% empty chance
        int featureAttempts = 12 + random.nextInt(8);
        for (int i = 0; i < featureAttempts; i++) {
            String featurePath = selectFromPool(FEATURES, random);
            if (featurePath != null) {
                // Place within 80 block radius
                int distance = 15 + random.nextInt(65);
                double angle = random.nextDouble() * Math.PI * 2;
                int offsetX = (int)(Math.cos(angle) * distance);
                int offsetZ = (int)(Math.sin(angle) * distance);
                
                BlockPos featurePos = pos.offset(offsetX, 0, offsetZ);
                placeStructure(serverLevel, templateManager, featurePath, featurePos,
                             Rotation.getRandom(random), random);
            }
        }
        
        // Spawn Wastelord boss at top of main tower
        BlockPos bossSpawnPos = pos.offset(8, 12, 8);
        PillagerOutpostIntegration.spawnWastelord(serverLevel, bossSpawnPos);
        
        return true;
    }
    
    /**
     * Places a single structure template
     */
    private static boolean placeStructure(ServerLevel level, StructureTemplateManager templateManager,
                                         String structurePath, BlockPos pos, Rotation rotation,
                                         RandomSource random) {
        ResourceLocation location = new ResourceLocation("lostcities", structurePath);
        StructureTemplate template = loadTemplate(templateManager, location);
        
        if (template == null) {
            return false;
        }
        
        Mirror mirror = random.nextBoolean() ? Mirror.NONE : Mirror.FRONT_BACK;
        
        // Validate position is above minimum world height
        int minY = level.getMinBuildHeight();
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
        // Create a filtered entity list before placement
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
                template.placeInWorld(level, placementPos, placementPos, settings, random, 2);
                // Restore original entities
                entitiesField.set(template, originalEntities);
            } else {
                template.placeInWorld(level, finalPos, finalPos, settings, random, 2);
            }
        } catch (Exception e) {
            // If reflection fails, place normally (will still have hanging entity errors)
            LostCities.LOGGER.warn("Failed to filter entities, placing structure normally: " + e.getMessage());
            template.placeInWorld(level, finalPos, finalPos, settings, random, 2);
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
            LostCities.LOGGER.error("Failed to load structure template: " + location, e);
            return null;
        }
    }
    
    /**
     * Checks if chunk should contain an outpost
     */
    public static boolean shouldGenerateOutpost(ServerLevel level, ChunkPos chunkPos) {
        // Use chunk coordinates for deterministic random
        Random random = new Random(level.getSeed() + 
                chunkPos.x * 341873128712L + 
                chunkPos.z * 132897987541L);
        
        // 1 in 500 chunk chance
        return random.nextInt(500) == 0;
    }
    
    /**
     * Gets the spawn position for outpost in chunk
     */
    public static BlockPos getOutpostPosition(ServerLevel level, ChunkPos chunkPos) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        
        // Center of chunk
        int x = chunkPos.getMinBlockX() + 8;
        int z = chunkPos.getMinBlockZ() + 8;
        
        // Find ground level
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            pos.set(x, y, z);
            if (!level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
                return pos.above().immutable();
            }
        }
        
        return new BlockPos(x, 64, z); // Default height if not found
    }
}
