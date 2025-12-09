package mcjty.lostcities.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * Structure processor that prevents blocks from spawning below minimum world height
 * Note: Entity filtering is done via reflection in structure placers
 */
public class SafeEntityProcessor extends StructureProcessor {
    
    @SuppressWarnings("unchecked")
    public static final Codec<SafeEntityProcessor> CODEC = (Codec<SafeEntityProcessor>) (Codec<?>) Codec.unit(SafeEntityProcessor::new);
    public static final SafeEntityProcessor INSTANCE = new SafeEntityProcessor();
    public static final StructureProcessorType<SafeEntityProcessor> TYPE = 
            () -> CODEC;
    
    private SafeEntityProcessor() {}
    
    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos seedPos, 
                                                             BlockPos structurePos, 
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings settings) {
        // Calculate final position in world
        BlockPos finalPos = blockInfoGlobal.pos();
        int minY = level.getMinBuildHeight();
        
        // If block would be placed below minimum height, skip it
        if (finalPos.getY() < minY) {
            return null;
        }
        
        return blockInfoGlobal;
    }
    
    @Override
    protected StructureProcessorType<?> getType() {
        return TYPE;
    }
}
