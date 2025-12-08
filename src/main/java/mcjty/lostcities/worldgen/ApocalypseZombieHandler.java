package mcjty.lostcities.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

/**
 * Apocalypse Zombie Mechanics Handler
 * Implements zombie apocalypse features from Apocalypse Overwrite datapack:
 * - Zombies break through blocks to reach players
 * - Dynamic difficulty scaling based on progression
 * - Zombie lunge attacks when grouped
 * - Speed scaling based on world difficulty
 * - Body odor mechanic (rotten flesh affects zombies)
 */
@Mod.EventBusSubscriber(modid = "lostcities")
public class ApocalypseZombieHandler {
    
    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;
    
    /**
     * Main tick handler for apocalypse zombie mechanics
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        tickCounter++;
        
        // Run every tick for critical mechanics
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!isLostCitiesDimension(level)) continue;
            
            // Process all zombies in the dimension
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Zombie zombie && zombie.isAlive()) {
                    // Update zombie attributes based on difficulty
                    if (tickCounter % 200 == 0) { // Every 10 seconds
                        updateZombieAttributes(zombie, level);
                    }
                    
                    // Lunge attack when near other zombies (1 in 30 chance per tick)
                    if (RANDOM.nextInt(30) == 0) {
                        attemptZombieLunge(zombie, level);
                    }
                    
                    // Block breaking mechanic
                    if (zombie.getTarget() instanceof Player) {
                        attemptBlockBreak(zombie, level);
                    }
                }
            }
            
            // Body odor effect for players holding rotten flesh
            for (Player player : level.players()) {
                handleBodyOdorMechanic(player, level);
            }
        }
    }
    
    /**
     * Updates zombie movement speed based on world difficulty
     * Easy: 0.18 (slower than default 0.23)
     * Normal: 0.23 (default speed)
     * Hard: 0.30 (30% faster)
     */
    private static void updateZombieAttributes(Zombie zombie, Level level) {
        Difficulty difficulty = level.getDifficulty();
        double speed;
        
        switch (difficulty) {
            case EASY -> speed = 0.18;
            case NORMAL -> speed = 0.23;
            case HARD -> speed = 0.30;
            default -> speed = 0.23;
        }
        
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                .setBaseValue(speed);
    }
    
    /**
     * Zombie lunge attack - launches zombie toward target when near other zombies
     */
    private static void attemptZombieLunge(Zombie zombie, Level level) {
        // Check if other zombies are nearby (within 2 blocks, but not too close)
        AABB searchBox = zombie.getBoundingBox().inflate(2.0);
        List<Zombie> nearbyZombies = level.getEntitiesOfClass(Zombie.class, searchBox,
                z -> z != zombie && z.distanceTo(zombie) > 0.1 && z.distanceTo(zombie) < 2.0);
        
        if (!nearbyZombies.isEmpty() && zombie.getTarget() != null) {
            // Launch zombie toward target
            Vec3 direction = zombie.getTarget().position().subtract(zombie.position()).normalize();
            zombie.setDeltaMovement(direction.x * 0.8, 0.4, direction.z * 0.8);
            zombie.hasImpulse = true;
            
            // Play attack sound
            level.playSound(null, zombie.blockPosition(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 
                    SoundSource.HOSTILE, 0.8f, 1.0f);
        }
    }
    
    /**
     * Zombies can break through blocks to reach players
     * Weak blocks (dirt, grass, etc.): 1 in 30 chance
     * Resistant blocks (stone, wood, etc.): 1 in 70 chance
     * Immune blocks (obsidian, bedrock, etc.): Cannot break
     */
    private static void attemptBlockBreak(Zombie zombie, Level level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;
        
        // Check block in front of zombie (at eye level and feet level)
        Direction facing = zombie.getDirection();
        BlockPos eyePos = zombie.blockPosition().relative(facing).above();
        BlockPos feetPos = zombie.blockPosition().relative(facing);
        
        tryBreakBlock(zombie, level, eyePos);
        tryBreakBlock(zombie, level, feetPos);
        
        // Also try block above zombie (when looking up)
        BlockPos abovePos = zombie.blockPosition().above();
        if (zombie.getTarget() != null && zombie.getTarget().getY() > zombie.getY() + 2) {
            tryBreakBlock(zombie, level, abovePos);
        }
    }
    
    /**
     * Attempts to break a specific block
     */
    private static void tryBreakBlock(Zombie zombie, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        // Immune blocks - cannot break
        if (isImmuneBlock(block)) {
            return;
        }
        
        // Play door attack sound
        level.playSound(null, pos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 
                SoundSource.HOSTILE, 0.4f, 1.0f);
        
        // Resistant blocks - harder to break (1 in 70 chance)
        if (isResistantBlock(block)) {
            if (RANDOM.nextInt(70) == 0) {
                level.destroyBlock(pos, true);
            }
        } 
        // Normal blocks - easier to break (1 in 30 chance)
        else {
            if (RANDOM.nextInt(30) == 0) {
                level.destroyBlock(pos, true);
            }
        }
    }
    
    /**
     * Body odor mechanic - rotten flesh weakens nearby zombies
     * If player has rotten flesh in both hands, nearby zombies get weakness
     */
    private static void handleBodyOdorMechanic(Player player, Level level) {
        // Check if player has rotten flesh in main hand and offhand
        boolean hasRottenFlesh = player.getMainHandItem().is(Items.ROTTEN_FLESH) && 
                                 player.getOffhandItem().is(Items.ROTTEN_FLESH);
        
        if (hasRottenFlesh) {
            // Apply weakness to 5 nearest zombies
            AABB searchBox = player.getBoundingBox().inflate(10.0);
            List<Zombie> nearbyZombies = level.getEntitiesOfClass(Zombie.class, searchBox);
            nearbyZombies.stream()
                    .sorted((z1, z2) -> Double.compare(z1.distanceTo(player), z2.distanceTo(player)))
                    .limit(5)
                    .forEach(z -> z.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 0, false, true)));
            
            // Particle effect
            if (RANDOM.nextInt(10) == 0) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SNEEZE,
                        player.getX() + (RANDOM.nextDouble() - 0.5) * 2,
                        player.getY() + 1 + (RANDOM.nextDouble() - 0.5) * 2,
                        player.getZ() + (RANDOM.nextDouble() - 0.5) * 2,
                        0, 0.01, 0);
            }
        }
    }
    
    /**
     * Blocks that zombies cannot break
     */
    private static boolean isImmuneBlock(Block block) {
        return block == Blocks.OBSIDIAN ||
               block == Blocks.BEDROCK ||
               block == Blocks.BARRIER ||
               block == Blocks.END_PORTAL_FRAME ||
               block == Blocks.END_PORTAL ||
               block == Blocks.NETHER_PORTAL ||
               block == Blocks.COMMAND_BLOCK ||
               block == Blocks.CHAIN_COMMAND_BLOCK ||
               block == Blocks.REPEATING_COMMAND_BLOCK ||
               block == Blocks.STRUCTURE_BLOCK ||
               block == Blocks.JIGSAW ||
               block == Blocks.SPAWNER ||
               block.defaultBlockState().isAir() ||
               block.defaultBlockState().liquid();
    }
    
    /**
     * Blocks that are harder for zombies to break
     */
    private static boolean isResistantBlock(Block block) {
        return block == Blocks.STONE ||
               block == Blocks.COBBLESTONE ||
               block == Blocks.DEEPSLATE ||
               block == Blocks.STONE_BRICKS ||
               block == Blocks.BRICKS ||
               block == Blocks.PRISMARINE ||
               block == Blocks.IRON_BLOCK ||
               block == Blocks.GOLD_BLOCK ||
               block == Blocks.DIAMOND_BLOCK ||
               block == Blocks.NETHERITE_BLOCK ||
               block == Blocks.IRON_DOOR ||
               block == Blocks.IRON_TRAPDOOR ||
               block.defaultBlockState().toString().contains("log") ||
               block.defaultBlockState().toString().contains("planks") ||
               block.defaultBlockState().toString().contains("door");
    }
    
    /**
     * Checks if dimension is Lost Cities
     */
    private static boolean isLostCitiesDimension(Level level) {
        return level.dimension().location().toString().contains("lostcities");
    }
}
