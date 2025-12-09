package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for haunted building mechanics
 */
@Mod.EventBusSubscriber(modid = LostCities.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HauntedBuildingEvents {
    
    /**
     * Handle chest interaction - lock chests in haunted buildings
     */
    @SubscribeEvent
    public static void onChestOpen(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        
        BlockPos pos = event.getPos();
        if (!(event.getLevel().getBlockEntity(pos) instanceof ChestBlockEntity)) {
            return;
        }
        
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        
        HauntedBuildingHandler handler = HauntedBuildingHandler.get(serverLevel);
        BuildingData data = handler.getBuildingData(chunkX, chunkZ, false);
        
        if (data != null && data.isHaunted()) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("The chest is sealed by dark magic! Clear the building first.").withStyle(ChatFormatting.RED));
        }
    }
    
    /**
     * Handle mob death - track kills in haunted buildings
     */
    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) {
            return;
        }
        
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        HauntedBuildingHandler handler = HauntedBuildingHandler.get(serverLevel);
        handler.onMobKilled(event.getEntity(), killer);
    }
    
    /**
     * Handle server tick - spawn mobs in haunted buildings
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        // Get overworld
        ServerLevel overworld = event.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        
        HauntedBuildingHandler handler = HauntedBuildingHandler.get(overworld);
        handler.onServerTick(overworld, overworld.players());
    }
}
