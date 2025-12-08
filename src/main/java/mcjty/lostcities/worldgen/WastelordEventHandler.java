package mcjty.lostcities.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Event handler for Wastelord boss deaths
 * Handles custom loot drops and player rewards
 */
@Mod.EventBusSubscriber(modid = "lostcities")
public class WastelordEventHandler {
    
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        
        if (level.isClientSide()) return;
        
        // Check if entity is a Wastelord boss
        if (entity.getTags().contains("evoker_wastelord")) {
            handleEvokerWastelordDeath((Evoker) entity, (ServerLevel) level);
        } else if (entity.getTags().contains("illusioner_wastelord")) {
            handleIllusionerWastelordDeath((Illusioner) entity, (ServerLevel) level);
        }
    }
    
    /**
     * Handle Evoker Wastelord death - drop custom loot
     */
    private static void handleEvokerWastelordDeath(Evoker evoker, ServerLevel level) {
        // Drop guaranteed Wastelord's Totem (custom totem)
        ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
        CompoundTag display = totem.getOrCreateTagElement("display");
        display.putString("Name", "{\"text\":\"Wastelord's Totem\",\"color\":\"gold\",\"bold\":true}");
        
        ListTag lore = new ListTag();
        lore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"A powerful totem taken from\",\"color\":\"gray\",\"italic\":true}"));
        lore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"a fallen Evoker Wastelord\",\"color\":\"gray\",\"italic\":true}"));
        display.put("Lore", lore);
        
        ItemEntity totemEntity = new ItemEntity(level, evoker.getX(), evoker.getY() + 1, evoker.getZ(), totem);
        level.addFreshEntity(totemEntity);
        
        // Drop Wastelord's Blade (powerful wooden sword)
        ItemStack blade = new ItemStack(Items.WOODEN_SWORD);
        blade.enchant(Enchantments.SHARPNESS, 10);
        blade.enchant(Enchantments.UNBREAKING, 5);
        blade.enchant(Enchantments.MENDING, 1);
        
        CompoundTag bladeDisplay = blade.getOrCreateTagElement("display");
        bladeDisplay.putString("Name", "{\"text\":\"Wastelord's Blade\",\"color\":\"dark_purple\",\"bold\":true}");
        
        ListTag bladeLore = new ListTag();
        bladeLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"An ancient blade of deception\",\"color\":\"gray\",\"italic\":true}"));
        bladeLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"that appears weak but strikes\",\"color\":\"gray\",\"italic\":true}"));
        bladeLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"with devastating power\",\"color\":\"gray\",\"italic\":true}"));
        bladeLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"\"}"));
        bladeLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"Enchantments:\",\"color\":\"gray\"}"));
        bladeLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\" Sharpness X\",\"color\":\"blue\"}"));
        bladeLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\" Unbreaking V\",\"color\":\"blue\"}"));
        bladeLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\" Mending I\",\"color\":\"blue\"}"));
        bladeDisplay.put("Lore", bladeLore);
        
        ItemEntity bladeEntity = new ItemEntity(level, evoker.getX(), evoker.getY() + 1, evoker.getZ(), blade);
        level.addFreshEntity(bladeEntity);
        
        // Announce defeat
        announceWastelordDefeat(level, evoker.blockPosition(), "Evoker Wastelord has been slain!", 0x8B00FF);
        
        // Play victory sound
        level.playSound(null, evoker.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.HOSTILE, 2.0f, 1.0f);
    }
    
    /**
     * Handle Illusioner Wastelord death - drop custom loot
     */
    private static void handleIllusionerWastelordDeath(Illusioner illusioner, ServerLevel level) {
        // Drop guaranteed Wastelord's Totem (custom totem)
        ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
        CompoundTag display = totem.getOrCreateTagElement("display");
        display.putString("Name", "{\"text\":\"Wastelord's Totem\",\"color\":\"gold\",\"bold\":true}");
        
        ListTag lore = new ListTag();
        lore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"A powerful totem taken from\",\"color\":\"gray\",\"italic\":true}"));
        lore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"a fallen Illusioner Wastelord\",\"color\":\"gray\",\"italic\":true}"));
        display.put("Lore", lore);
        
        ItemEntity totemEntity = new ItemEntity(level, illusioner.getX(), illusioner.getY() + 1, illusioner.getZ(), totem);
        level.addFreshEntity(totemEntity);
        
        // Drop Wastelord's Deception (powerful bow)
        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 6);
        bow.enchant(Enchantments.INFINITY_ARROWS, 1);
        bow.enchant(Enchantments.UNBREAKING, 5);
        bow.enchant(Enchantments.MENDING, 1);
        
        CompoundTag bowDisplay = bow.getOrCreateTagElement("display");
        bowDisplay.putString("Name", "{\"text\":\"Wastelord's Deception\",\"color\":\"aqua\",\"bold\":true}");
        
        ListTag bowLore = new ListTag();
        bowLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"A bow that bends reality\",\"color\":\"gray\",\"italic\":true}"));
        bowLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"with every arrow fired\",\"color\":\"gray\",\"italic\":true}"));
        bowLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"\"}"));
        bowLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"Enchantments:\",\"color\":\"gray\"}"));
        bowLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\" Power VI\",\"color\":\"blue\"}"));
        bowLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\" Infinity I\",\"color\":\"blue\"}"));
        bowLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\" Unbreaking V\",\"color\":\"blue\"}"));
        bowLore.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\" Mending I\",\"color\":\"blue\"}"));
        bowDisplay.put("Lore", bowLore);
        
        ItemEntity bowEntity = new ItemEntity(level, illusioner.getX(), illusioner.getY() + 1, illusioner.getZ(), bow);
        level.addFreshEntity(bowEntity);
        
        // Announce defeat
        announceWastelordDefeat(level, illusioner.blockPosition(), "Illusioner Wastelord has been slain!", 0x00FFFF);
        
        // Play victory sound
        level.playSound(null, illusioner.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.HOSTILE, 2.0f, 1.0f);
    }
    
    /**
     * Announces Wastelord defeat to nearby players
     */
    private static void announceWastelordDefeat(Level level, net.minecraft.core.BlockPos pos, String message, int color) {
        AABB searchBox = new AABB(pos).inflate(100);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox);
        
        Component announcement = Component.literal(message)
                .withStyle(style -> style.withColor(color).withBold(true));
        
        for (Player player : nearbyPlayers) {
            player.sendSystemMessage(announcement);
        }
    }
}
