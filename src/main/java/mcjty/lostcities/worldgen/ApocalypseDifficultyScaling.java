package mcjty.lostcities.worldgen;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Apocalypse Difficulty Scaling System
 * Dynamically spawns zombies based on player progression and difficulty
 * The more advanced players are, the more zombies spawn around them
 */
@Mod.EventBusSubscriber(modid = "lostcities")
public class ApocalypseDifficultyScaling {
    
    private static final Random RANDOM = new Random();
    private static final Map<UUID, Integer> playerZombieCounts = new HashMap<>();
    private static final Map<UUID, Integer> playerZombieLimits = new HashMap<>();
    private static int tickCounter = 0;
    
    /**
     * Progression-based advancement checks
     * Each advancement increases zombie spawn limit
     */
    private static final Map<String, Integer> ADVANCEMENT_ZOMBIE_INCREASES = new HashMap<>();
    
    static {
        // Base progression
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:story/smelt_iron", 20);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:story/mine_diamond", 20);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:story/enchant_item", 20);
        
        // Nether progression
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:nether/obtain_blaze_rod", 20);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:nether/obtain_ancient_debris", 20);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:nether/netherite_armor", 20);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:nether/create_beacon", 30);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:nether/create_full_beacon", 20);
        
        // Adventure progression
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:adventure/sleep_in_bed", 5);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:adventure/trade", 10);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:adventure/hero_of_the_village", 30);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:adventure/throw_trident", 15);
        
        // End progression
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:end/dragon_egg", 20);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:end/elytra", 30);
        ADVANCEMENT_ZOMBIE_INCREASES.put("minecraft:end/respawn_dragon", 20);
    }
    
    /**
     * Main tick handler for apocalypse zombie spawning
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        tickCounter++;
        
        // Run every 8 ticks (1 in 8 chance matches datapack)
        if (tickCounter % 8 != 0) return;
        
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!isLostCitiesDimension(level)) continue;
            if (level.getDifficulty() == Difficulty.PEACEFUL) continue;
            
            for (ServerPlayer player : level.players()) {
                // Calculate player's zombie limit based on progression
                int zombieLimit = calculateZombieLimit(player, level);
                playerZombieLimits.put(player.getUUID(), zombieLimit);
                
                // Count zombies around player
                int zombieCount = countNearbyZombies(player, level);
                playerZombieCounts.put(player.getUUID(), zombieCount);
                
                // Spawn zombies if under limit
                if (zombieCount < zombieLimit) {
                    spawnApocalypseZombies(player, level);
                }
            }
        }
    }
    
    /**
     * Calculates zombie spawn limit for a player based on their progression
     */
    private static int calculateZombieLimit(ServerPlayer player, ServerLevel level) {
        int baseLimit = 20; // Starting limit
        
        // Check all progression advancements
        for (Map.Entry<String, Integer> entry : ADVANCEMENT_ZOMBIE_INCREASES.entrySet()) {
            Advancement advancement = level.getServer().getAdvancements()
                    .getAdvancement(new ResourceLocation(entry.getKey()));
            
            if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                baseLimit += entry.getValue();
            }
        }
        
        // Difficulty modifier
        Difficulty difficulty = level.getDifficulty();
        switch (difficulty) {
            case NORMAL -> baseLimit += 0; // No change
            case HARD -> baseLimit += 20; // +20 zombies on hard
            default -> baseLimit += 0;
        }
        
        // Global cap based on player count (prevents server overload)
        // 400 total zombies divided by number of players
        int playerCount = level.players().size();
        int globalCap = playerCount > 0 ? 400 / playerCount : 400;
        
        return Math.min(baseLimit, globalCap);
    }
    
    /**
     * Counts zombies within 128 blocks of player
     */
    private static int countNearbyZombies(ServerPlayer player, Level level) {
        AABB searchBox = player.getBoundingBox().inflate(128.0);
        List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, searchBox);
        return zombies.size();
    }
    
    /**
     * Spawns zombies around player in apocalypse style
     * Spawns on surface and underground
     */
    private static void spawnApocalypseZombies(ServerPlayer player, ServerLevel level) {
        // Surface spawn
        if (RANDOM.nextBoolean()) {
            BlockPos surfaceSpawn = findSurfaceSpawnPosition(player, level);
            if (surfaceSpawn != null) {
                spawnZombie(level, surfaceSpawn);
            }
        }
        
        // Underground spawn (under y=50)
        if (RANDOM.nextBoolean()) {
            BlockPos undergroundSpawn = findUndergroundSpawnPosition(player, level);
            if (undergroundSpawn != null) {
                spawnZombie(level, undergroundSpawn);
            }
        }
    }
    
    /**
     * Finds a valid surface spawn position near player
     */
    private static BlockPos findSurfaceSpawnPosition(ServerPlayer player, Level level) {
        // Random position within 128 blocks of player
        int offsetX = RANDOM.nextInt(256) - 128;
        int offsetZ = RANDOM.nextInt(256) - 128;
        
        BlockPos targetPos = player.blockPosition().offset(offsetX, 0, offsetZ);
        
        // Don't spawn too close to player (min 15 blocks)
        if (targetPos.distSqr(player.blockPosition()) < 15 * 15) {
            return null;
        }
        
        // Find ground level
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
            if (!level.getBlockState(checkPos).isAir() && level.getBlockState(checkPos.above()).isAir()) {
                return checkPos.above();
            }
        }
        
        return null;
    }
    
    /**
     * Finds a valid underground spawn position near player (under y=50)
     */
    private static BlockPos findUndergroundSpawnPosition(ServerPlayer player, Level level) {
        // Random position within 128 blocks of player
        int offsetX = RANDOM.nextInt(256) - 128;
        int offsetZ = RANDOM.nextInt(256) - 128;
        
        BlockPos targetPos = player.blockPosition().offset(offsetX, 0, offsetZ);
        
        // Don't spawn too close to player (min 15 blocks)
        if (targetPos.distSqr(player.blockPosition()) < 15 * 15) {
            return null;
        }
        
        // Find cave/underground space under y=50
        for (int y = 50; y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
            if (level.getBlockState(checkPos).isAir() && 
                !level.getBlockState(checkPos.below()).isAir() && 
                level.getBlockState(checkPos.above()).isAir()) {
                return checkPos;
            }
        }
        
        return null;
    }
    
    /**
     * Spawns a zombie at the specified position
     */
    private static void spawnZombie(ServerLevel level, BlockPos pos) {
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) return;
        
        zombie.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
        
        level.addFreshEntity(zombie);
    }
    
    /**
     * Checks if dimension is Lost Cities
     */
    private static boolean isLostCitiesDimension(Level level) {
        return level.dimension().location().toString().contains("lostcities");
    }
    
    /**
     * Gets current zombie limit for a player (for debugging/display)
     */
    public static int getPlayerZombieLimit(UUID playerUUID) {
        return playerZombieLimits.getOrDefault(playerUUID, 20);
    }
    
    /**
     * Gets current zombie count for a player (for debugging/display)
     */
    public static int getPlayerZombieCount(UUID playerUUID) {
        return playerZombieCounts.getOrDefault(playerUUID, 0);
    }
}
