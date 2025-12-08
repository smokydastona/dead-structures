package mcjty.lostcities.worldgen;

import com.mojang.serialization.Codec;
import mcjty.lostcities.LostCities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles placement of Pillager Outpost structures in Lost Cities
 */
public class PillagerOutpostStructurePlacer {
    
    private static final List<String> OUTPOST_STRUCTURES = new ArrayList<>();
    private static final List<String> FEATURE_STRUCTURES = new ArrayList<>();
    
    static {
        // Main outpost structures
        OUTPOST_STRUCTURES.add("pillager_outpost/pillager_centers/base_plate");
        
        // Feature structures (cages, tents, targets, etc.)
        FEATURE_STRUCTURES.add("pillager_outpost/feature_cage1");
        FEATURE_STRUCTURES.add("pillager_outpost/feature_cage2");
        FEATURE_STRUCTURES.add("pillager_outpost/feature_cage3");
        FEATURE_STRUCTURES.add("pillager_outpost/feature_cage_with_allays");
        FEATURE_STRUCTURES.add("pillager_outpost/feature_tent1");
        FEATURE_STRUCTURES.add("pillager_outpost/feature_tent2");
        FEATURE_STRUCTURES.add("pillager_outpost/feature_targets");
        FEATURE_STRUCTURES.add("pillager_outpost/feature_logs");
        FEATURE_STRUCTURES.add("pillager_outpost/watchtower");
        FEATURE_STRUCTURES.add("pillager_outpost/watchtower_overgrown");
    }
    
    /**
     * Places a complete Pillager Outpost structure with features
     */
    public static boolean placeOutpost(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        
        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        
        // Place main base plate
        ResourceLocation baseStructure = new ResourceLocation("lostcities", OUTPOST_STRUCTURES.get(0));
        StructureTemplate template = loadTemplate(templateManager, baseStructure);
        
        if (template == null) {
            return false;
        }
        
        // Create placement settings
        Rotation rotation = Rotation.getRandom(random);
        Mirror mirror = random.nextBoolean() ? Mirror.NONE : Mirror.FRONT_BACK;
        
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(mirror)
                .setRandom(random);
        
        // Place the structure
        template.placeInWorld(serverLevel, pos, pos, settings, random, 2);
        
        // Place random features around the outpost
        placeFeatures(serverLevel, pos, random, templateManager, rotation, mirror);
        
        // Spawn Wastelord boss at center
        BlockPos bossSpawnPos = pos.offset(8, 10, 8); // Adjust based on structure size
        PillagerOutpostIntegration.spawnWastelord(serverLevel, bossSpawnPos);
        
        return true;
    }
    
    /**
     * Places random features around the main outpost
     */
    private static void placeFeatures(ServerLevel level, BlockPos basePos, RandomSource random, 
                                     StructureTemplateManager templateManager, Rotation rotation, Mirror mirror) {
        int featureCount = 2 + random.nextInt(4); // 2-5 features
        
        for (int i = 0; i < featureCount; i++) {
            // Random feature
            String featurePath = FEATURE_STRUCTURES.get(random.nextInt(FEATURE_STRUCTURES.size()));
            ResourceLocation featureLocation = new ResourceLocation("lostcities", featurePath);
            StructureTemplate featureTemplate = loadTemplate(templateManager, featureLocation);
            
            if (featureTemplate == null) continue;
            
            // Random offset from base
            int offsetX = -20 + random.nextInt(40);
            int offsetZ = -20 + random.nextInt(40);
            BlockPos featurePos = basePos.offset(offsetX, 0, offsetZ);
            
            // Create placement settings
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(rotation)
                    .setMirror(mirror)
                    .setRandom(random);
            
            // Place feature
            featureTemplate.placeInWorld(level, featurePos, featurePos, settings, random, 2);
        }
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
