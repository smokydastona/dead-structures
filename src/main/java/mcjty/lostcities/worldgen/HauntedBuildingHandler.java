package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles haunted building spawning mechanics
 * Based on Lost Souls addon by McJty
 */
public class HauntedBuildingHandler extends SavedData {
    private static final String DATA_NAME = "lostcities_haunted_buildings";
    
    // Configuration
    private static final int SPAWN_TIMEOUT = 100; // Ticks between spawn attempts
    private static final int MIN_SPAWN_DISTANCE = 8; // Blocks away from player
    private static final int SPAWN_MAX_NEARBY = 3; // Max mobs of same type nearby
    private static final float MIN_HEALTH_BONUS = 1.5f;
    private static final float MAX_HEALTH_BONUS = 3.0f;
    private static final float MIN_DAMAGE_BONUS = 1.3f;
    private static final float MAX_DAMAGE_BONUS = 2.5f;
    private static final double HAUNTED_CHANCE = 0.7; // 70% of buildings are haunted
    
    // Mob types to spawn (vanilla + modded if available)
    private static final String[] SPAWN_MOBS = {
        "minecraft:zombie",
        "minecraft:skeleton",
        "minecraft:spider",
        "minecraft:cave_spider",
        "minecraft:creeper",
        "minecraft:zombie_villager",
        "minecraft:husk",
        "minecraft:stray",
        "minecraft:drowned"
    };
    
    // Equipment pools
    private static final String[] WEAPONS = {
        "minecraft:iron_sword",
        "minecraft:stone_sword",
        "minecraft:iron_axe",
        "minecraft:bow"
    };
    
    private static final String[] ARMOR_PIECES = {
        "minecraft:leather_helmet",
        "minecraft:leather_chestplate",
        "minecraft:leather_leggings",
        "minecraft:leather_boots",
        "minecraft:chainmail_helmet",
        "minecraft:chainmail_chestplate",
        "minecraft:chainmail_leggings",
        "minecraft:chainmail_boots",
        "minecraft:iron_helmet",
        "minecraft:iron_chestplate",
        "minecraft:iron_leggings",
        "minecraft:iron_boots"
    };
    
    private final Map<Long, BuildingData> buildingDataMap = new HashMap<>();
    private final Map<UUID, ChunkPos> playerChunks = new HashMap<>();
    private int spawnTimeout = SPAWN_TIMEOUT;
    
    public HauntedBuildingHandler() {
        super();
    }
    
    public HauntedBuildingHandler(CompoundTag tag) {
        super();
        ListTag buildings = tag.getList("buildings", 10);
        for (int i = 0; i < buildings.size(); i++) {
            CompoundTag buildingTag = buildings.getCompound(i);
            int chunkX = buildingTag.getInt("chunkX");
            int chunkZ = buildingTag.getInt("chunkZ");
            BuildingData data = new BuildingData(chunkX, chunkZ, buildingTag);
            buildingDataMap.put(ChunkPos.asLong(chunkX, chunkZ), data);
        }
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag buildings = new ListTag();
        for (BuildingData data : buildingDataMap.values()) {
            buildings.add(data.save());
        }
        tag.put("buildings", buildings);
        return tag;
    }
    
    public static HauntedBuildingHandler get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            HauntedBuildingHandler::new,
            HauntedBuildingHandler::new,
            DATA_NAME
        );
    }
    
    /**
     * Get or create building data for a chunk
     */
    public BuildingData getBuildingData(int chunkX, int chunkZ, boolean createIfMissing) {
        long key = ChunkPos.asLong(chunkX, chunkZ);
        BuildingData data = buildingDataMap.get(key);
        
        if (data == null && createIfMissing) {
            data = new BuildingData(chunkX, chunkZ);
            buildingDataMap.put(key, data);
            setDirty();
        }
        
        return data;
    }
    
    /**
     * Initialize a building as haunted
     */
    public void initializeHauntedBuilding(int chunkX, int chunkZ, RandomSource random) {
        if (random.nextDouble() < HAUNTED_CHANCE) {
            BuildingData data = getBuildingData(chunkX, chunkZ, true);
            data.setHaunted(true);
            // Set total mobs based on building size (15-40 mobs)
            data.setTotalMobs(15 + random.nextInt(26));
            setDirty();
        }
    }
    
    /**
     * Handle player chunk updates and spawning
     */
    public void onServerTick(ServerLevel level, List<ServerPlayer> players) {
        spawnTimeout--;
        if (spawnTimeout > 0) {
            return;
        }
        spawnTimeout = SPAWN_TIMEOUT;
        
        for (ServerPlayer player : players) {
            UUID uuid = player.getUUID();
            BlockPos position = player.blockPosition();
            int chunkX = position.getX() >> 4;
            int chunkZ = position.getZ() >> 4;
            ChunkPos currentChunk = new ChunkPos(chunkX, chunkZ);
            
            boolean entered = false;
            ChunkPos oldChunk = playerChunks.get(uuid);
            if (oldChunk == null || !oldChunk.equals(currentChunk)) {
                playerChunks.put(uuid, currentChunk);
                entered = true;
            }
            
            handleSpawn(level, player, chunkX, chunkZ, entered);
        }
    }
    
    /**
     * Handle mob spawning in haunted buildings
     */
    private void handleSpawn(ServerLevel level, ServerPlayer player, int chunkX, int chunkZ, boolean entered) {
        BuildingData data = getBuildingData(chunkX, chunkZ, false);
        
        if (data != null && data.isHaunted()) {
            if (entered) {
                data.enterBuilding();
                setDirty();
                
                int enteredCount = data.getEnteredCount();
                if (enteredCount == 1) {
                    player.sendSystemMessage(Component.literal("This building feels haunted...").withStyle(ChatFormatting.YELLOW));
                }
            }
            
            // Spawn mobs near player
            BlockPos position = player.blockPosition();
            RandomSource random = level.getRandom();
            
            double x = chunkX * 16 + random.nextDouble() * 16.0;
            double y = position.getY() + random.nextInt(3) - 1;
            double z = chunkZ * 16 + random.nextDouble() * 16.0;
            
            BlockPos spawnPos = new BlockPos((int) x, (int) y, (int) z);
            
            // Check if air block below
            if (level.isEmptyBlock(spawnPos.below())) {
                y--;
                spawnPos = new BlockPos((int) x, (int) y, (int) z);
            }
            
            // Check spawn validity
            if (!level.isEmptyBlock(spawnPos)) {
                return;
            }
            
            double distance = position.distSqr(spawnPos);
            if (distance < MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE) {
                return;
            }
            
            // Select random mob type
            String mobId = SPAWN_MOBS[random.nextInt(SPAWN_MOBS.length)];
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mobId));
            
            if (entityType == null) {
                return;
            }
            
            Entity entity = entityType.create(level);
            if (!(entity instanceof Mob mob)) {
                return;
            }
            
            // Check nearby mob count
            AABB checkBox = new AABB(spawnPos).inflate(8.0);
            int nearbyCount = level.getEntitiesOfClass(mob.getClass(), checkBox).size();
            if (nearbyCount > SPAWN_MAX_NEARBY) {
                return;
            }
            
            // Set position
            mob.moveTo(x, y, z, random.nextFloat() * 360.0f, 0.0f);
            
            // Boost mob stats and equipment
            boostMob(level, mob, random);
            
            // Tag mob for tracking
            mob.addTag("_haunted_:" + chunkX + ":" + chunkZ);
            
            // Spawn
            level.addFreshEntity(mob);
        }
    }
    
    /**
     * Boost mob with enhanced stats and equipment
     */
    private void boostMob(ServerLevel level, Mob mob, RandomSource random) {
        // Health boost
        AttributeInstance healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            float multiplier = MIN_HEALTH_BONUS + random.nextFloat() * (MAX_HEALTH_BONUS - MIN_HEALTH_BONUS);
            double newMax = healthAttr.getBaseValue() * multiplier;
            healthAttr.setBaseValue(newMax);
            mob.setHealth((float) newMax);
        }
        
        // Damage boost
        AttributeInstance damageAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            float multiplier = MIN_DAMAGE_BONUS + random.nextFloat() * (MAX_DAMAGE_BONUS - MIN_DAMAGE_BONUS);
            double newDamage = damageAttr.getBaseValue() * multiplier;
            damageAttr.setBaseValue(newDamage);
        }
        
        // Random potion effects (30% chance each)
        if (random.nextFloat() < 0.3f) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("minecraft:speed"));
            if (effect != null) {
                mob.addEffect(new MobEffectInstance(effect, 100000, random.nextInt(2)));
            }
        }
        
        if (random.nextFloat() < 0.3f) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("minecraft:strength"));
            if (effect != null) {
                mob.addEffect(new MobEffectInstance(effect, 100000, random.nextInt(2)));
            }
        }
        
        if (random.nextFloat() < 0.3f) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("minecraft:regeneration"));
            if (effect != null) {
                mob.addEffect(new MobEffectInstance(effect, 100000, 0));
            }
        }
        
        // Random equipment (40% chance each slot)
        if (random.nextFloat() < 0.4f) {
            String weaponId = WEAPONS[random.nextInt(WEAPONS.length)];
            Item weapon = ForgeRegistries.ITEMS.getValue(new ResourceLocation(weaponId));
            if (weapon != null) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
            }
        }
        
        if (random.nextFloat() < 0.4f) {
            String armorId = ARMOR_PIECES[random.nextInt(ARMOR_PIECES.length)];
            Item armor = ForgeRegistries.ITEMS.getValue(new ResourceLocation(armorId));
            if (armor != null) {
                mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(armor));
            }
        }
        
        if (random.nextFloat() < 0.4f) {
            String armorId = ARMOR_PIECES[random.nextInt(ARMOR_PIECES.length)];
            Item armor = ForgeRegistries.ITEMS.getValue(new ResourceLocation(armorId));
            if (armor != null) {
                mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(armor));
            }
        }
        
        if (random.nextFloat() < 0.4f) {
            String armorId = ARMOR_PIECES[random.nextInt(ARMOR_PIECES.length)];
            Item armor = ForgeRegistries.ITEMS.getValue(new ResourceLocation(armorId));
            if (armor != null) {
                mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(armor));
            }
        }
        
        if (random.nextFloat() < 0.4f) {
            String armorId = ARMOR_PIECES[random.nextInt(ARMOR_PIECES.length)];
            Item armor = ForgeRegistries.ITEMS.getValue(new ResourceLocation(armorId));
            if (armor != null) {
                mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(armor));
            }
        }
    }
    
    /**
     * Handle mob kill tracking
     */
    public void onMobKilled(Entity entity, ServerPlayer killer) {
        for (String tag : entity.getTags()) {
            if (tag.startsWith("_haunted_:")) {
                String[] parts = tag.split(":");
                try {
                    int chunkX = Integer.parseInt(parts[1]);
                    int chunkZ = Integer.parseInt(parts[2]);
                    
                    BuildingData data = getBuildingData(chunkX, chunkZ, false);
                    if (data != null) {
                        data.addKill();
                        setDirty();
                        
                        int killed = data.getNumberKilled();
                        int total = data.getTotalMobs();
                        
                        if (killed >= total) {
                            killer.sendSystemMessage(Component.literal("Building cleared! The haunting has lifted.").withStyle(ChatFormatting.GREEN));
                        } else if (killed == total / 2) {
                            killer.sendSystemMessage(Component.literal("Halfway there! " + killed + "/" + total + " mobs defeated.").withStyle(ChatFormatting.YELLOW));
                        }
                    }
                } catch (NumberFormatException e) {
                    LostCities.LOGGER.error("Failed to parse haunted building tag: " + tag);
                }
                break;
            }
        }
    }
    
    /**
     * Helper class for chunk coordinates
     */
    private static class ChunkPos {
        final int x;
        final int z;
        
        ChunkPos(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        static long asLong(int x, int z) {
            return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ChunkPos other)) {
                return false;
            }
            return this.x == other.x && this.z == other.z;
        }
        
        @Override
        public int hashCode() {
            return x * 31 + z;
        }
    }
}
