package mcjty.lostcities.worldgen;

import net.minecraft.nbt.CompoundTag;

/**
 * Tracks haunted building state - mob kills, player entries, cleared status
 */
public class BuildingData {
    private final int chunkX;
    private final int chunkZ;
    private boolean haunted;
    private int totalMobs;
    private int numberKilled;
    private int enteredCount;
    private boolean cleared;
    
    public BuildingData(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.haunted = false;
        this.totalMobs = 0;
        this.numberKilled = 0;
        this.enteredCount = 0;
        this.cleared = false;
    }
    
    public BuildingData(int chunkX, int chunkZ, CompoundTag tag) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.haunted = tag.getBoolean("haunted");
        this.totalMobs = tag.getInt("totalMobs");
        this.numberKilled = tag.getInt("numberKilled");
        this.enteredCount = tag.getInt("enteredCount");
        this.cleared = tag.getBoolean("cleared");
    }
    
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("chunkX", chunkX);
        tag.putInt("chunkZ", chunkZ);
        tag.putBoolean("haunted", haunted);
        tag.putInt("totalMobs", totalMobs);
        tag.putInt("numberKilled", numberKilled);
        tag.putInt("enteredCount", enteredCount);
        tag.putBoolean("cleared", cleared);
        return tag;
    }
    
    public void setHaunted(boolean haunted) {
        this.haunted = haunted;
    }
    
    public boolean isHaunted() {
        return haunted && !cleared;
    }
    
    public void setTotalMobs(int totalMobs) {
        this.totalMobs = totalMobs;
    }
    
    public int getTotalMobs() {
        return totalMobs;
    }
    
    public int getNumberKilled() {
        return numberKilled;
    }
    
    public void addKill() {
        numberKilled++;
        if (numberKilled >= totalMobs) {
            cleared = true;
        }
    }
    
    public void enterBuilding() {
        enteredCount++;
    }
    
    public int getEnteredCount() {
        return enteredCount;
    }
    
    public boolean isCleared() {
        return cleared;
    }
    
    public int getChunkX() {
        return chunkX;
    }
    
    public int getChunkZ() {
        return chunkZ;
    }
}
