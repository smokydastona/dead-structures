package mcjty.lostcities.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

/**
 * Pillager Outpost Integration for Lost Cities
 * Adds enhanced pillager outposts with Wastelord bosses to the Lost Cities dimension
 */
public class PillagerOutpostIntegration {
    
    private static final String EVOKER_WASTELORD_TAG = "evoker_wastelord";
    private static final String ILLUSIONER_WASTELORD_TAG = "illusioner_wastelord";
    
    /**
     * Spawns a Wastelord boss at the specified location
     * 50% chance for Evoker Wastelord, 50% chance for Illusioner Wastelord
     */
    public static void spawnWastelord(ServerLevel level, BlockPos pos) {
        Random random = level.getRandom();
        
        if (random.nextBoolean()) {
            spawnEvokerWastelord(level, pos);
        } else {
            spawnIllusionerWastelord(level, pos);
        }
    }
    
    /**
     * Spawns an Evoker Wastelord boss
     */
    private static void spawnEvokerWastelord(ServerLevel level, BlockPos pos) {
        Evoker evoker = EntityType.EVOKER.create(level);
        if (evoker == null) return;
        
        // Set position
        evoker.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        
        // Set custom name
        evoker.setCustomName(Component.literal("Evoker Wastelord")
                .withStyle(style -> style.withColor(0x8B00FF).withBold(true)));
        evoker.setCustomNameVisible(true);
        
        // Make persistent
        evoker.setPersistenceRequired();
        
        // BOSS STATS - Make extremely powerful
        evoker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(500.0); // 250 hearts
        evoker.setHealth(500.0f);
        evoker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(20.0); // Diamond armor tier
        evoker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS).setBaseValue(12.0);
        evoker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0); // Immune to knockback
        evoker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE).setBaseValue(64.0); // Extended detection
        
        // Add custom weapon - Wastelord's Blade (wooden sword with Sharpness X)
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 10);
        sword.enchant(Enchantments.UNBREAKING, 5);
        sword.enchant(Enchantments.MENDING, 1);
        sword.enchant(Enchantments.FIRE_ASPECT, 2);
        sword.enchant(Enchantments.SWEEPING_EDGE, 3);
        CompoundTag display = sword.getOrCreateTagElement("display");
        display.putString("Name", "{\"text\":\"Wastelord's Blade\",\"color\":\"dark_purple\",\"bold\":true}");
        evoker.setItemSlot(EquipmentSlot.MAINHAND, sword);
        
        // Add boss effects - SIGNIFICANTLY BUFFED
        evoker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 5, false, false)); // +150% damage
        evoker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 3, false, false)); // -60% damage taken
        evoker.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        evoker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 1, false, false)); // 40% faster
        evoker.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 2, false, false)); // Strong regen
        evoker.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false)); // Immune to fire
        
        // Add boss tag
        evoker.addTag(EVOKER_WASTELORD_TAG);
        evoker.addTag("boss");
        evoker.addTag("custom_death_loot");
        
        // Spawn entity
        level.addFreshEntity(evoker);
        
        // Announce spawn
        announceWastelordSpawn(level, pos, "An Evoker Wastelord has appeared!", 0x8B00FF);
        
        // Play sound
        level.playSound(null, pos, SoundEvents.EVOKER_AMBIENT, SoundSource.HOSTILE, 2.0f, 0.8f);
    }
    
    /**
     * Spawns an Illusioner Wastelord boss
     */
    private static void spawnIllusionerWastelord(ServerLevel level, BlockPos pos) {
        Illusioner illusioner = EntityType.ILLUSIONER.create(level);
        if (illusioner == null) return;
        
        // Set position
        illusioner.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        
        // Set custom name
        illusioner.setCustomName(Component.literal("Illusioner Wastelord")
                .withStyle(style -> style.withColor(0x00FFFF).withBold(true)));
        illusioner.setCustomNameVisible(true);
        
        // Make persistent
        illusioner.setPersistenceRequired();
        
        // BOSS STATS - Make extremely powerful
        illusioner.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(450.0); // 225 hearts
        illusioner.setHealth(450.0f);
        illusioner.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(18.0);
        illusioner.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS).setBaseValue(10.0);
        illusioner.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0); // Immune to knockback
        illusioner.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE).setBaseValue(80.0); // Very long range for archer
        
        // Add custom weapon - Enchanted Bow (Power VI, Infinity)
        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 6);
        bow.enchant(Enchantments.INFINITY_ARROWS, 1);
        bow.enchant(Enchantments.UNBREAKING, 5);
        bow.enchant(Enchantments.MENDING, 1);
        bow.enchant(Enchantments.PUNCH_ARROWS, 2);
        bow.enchant(Enchantments.FLAMING_ARROWS, 1);
        CompoundTag display = bow.getOrCreateTagElement("display");
        display.putString("Name", "{\"text\":\"Wastelord's Deception\",\"color\":\"aqua\",\"bold\":true}");
        illusioner.setItemSlot(EquipmentSlot.MAINHAND, bow);
        
        // Add arrows to offhand
        illusioner.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ARROW));
        
        // Add boss effects - SIGNIFICANTLY BUFFED
        illusioner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 4, false, false)); // +120% damage
        illusioner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 3, false, false)); // -60% damage taken
        illusioner.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        illusioner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 2, false, false)); // 60% faster
        illusioner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 2, false, false)); // Strong regen
        illusioner.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false)); // Permanent invisibility (illusioner theme)
        
        // Add boss tag
        illusioner.addTag(ILLUSIONER_WASTELORD_TAG);
        illusioner.addTag("boss");
        illusioner.addTag("custom_death_loot");
        
        // Spawn entity
        level.addFreshEntity(illusioner);
        
        // Announce spawn
        announceWastelordSpawn(level, pos, "An Illusioner Wastelord has appeared!", 0x00FFFF);
        
        // Play sound
        level.playSound(null, pos, SoundEvents.ILLUSIONER_AMBIENT, SoundSource.HOSTILE, 2.0f, 0.8f);
    }
    
    /**
     * Announces Wastelord spawn to nearby players
     */
    private static void announceWastelordSpawn(Level level, BlockPos pos, String message, int color) {
        AABB searchBox = new AABB(pos).inflate(50);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox);
        
        Component announcement = Component.literal(message)
                .withStyle(style -> style.withColor(color).withBold(true));
        
        for (Player player : nearbyPlayers) {
            player.sendSystemMessage(announcement);
        }
    }
    
    /**
     * Checks if outpost should spawn at this location
     * Criteria: Lost Cities dimension, appropriate biome, random chance
     */
    public static boolean shouldSpawnOutpost(Level level, BlockPos pos) {
        // Only spawn in Lost Cities dimension
        if (!level.dimension().location().toString().contains("lostcities")) {
            return false;
        }
        
        // Random chance (e.g., 1 in 500 chunks)
        Random random = new Random(level.getSeed() + pos.getX() * 341873128712L + pos.getZ() * 132897987541L);
        return random.nextInt(500) == 0;
    }
    
    /**
     * Gets the recommended spawn height for outpost
     */
    public static int getOutpostSpawnHeight(Level level, BlockPos pos) {
        // Find highest solid block
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!level.getBlockState(checkPos).isAir() && level.getBlockState(checkPos.above()).isAir()) {
                return y + 1;
            }
        }
        return pos.getY();
    }
}
