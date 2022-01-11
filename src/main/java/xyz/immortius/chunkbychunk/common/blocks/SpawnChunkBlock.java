package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xyz.immortius.chunkbychunk.common.config.ChunkByChunkConfig;

/**
 * Spawns a chunk from the equivalent chunk in the source dimension (with configuration offset)
 */
public class SpawnChunkBlock extends BaseSpawnChunkBlock {

    public SpawnChunkBlock(Properties blockProperties) {
        super(blockProperties);
    }

    @Override
    protected ChunkPos getSourceChunk(Level targetLevel, BlockPos targetBlockPos) {
        ChunkPos pos = new ChunkPos(targetBlockPos);
        return new ChunkPos(pos.x + ChunkByChunkConfig.chunkGenXOffset.get(), pos.z + ChunkByChunkConfig.chunkGenZOffset.get());
    }

}
