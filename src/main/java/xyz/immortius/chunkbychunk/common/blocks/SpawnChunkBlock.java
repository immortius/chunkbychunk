package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

/**
 * Spawns a chunk from the equivalent chunk in the source dimension (with configuration offset)
 */
public class SpawnChunkBlock extends BaseSpawnChunkBlock {

    public SpawnChunkBlock(Properties blockProperties) {
        super(blockProperties);
    }

    @Override
    public BlockState getTriggeredBlockState() {
        return ChunkByChunkConstants.triggeredSpawnChunkBlock().defaultBlockState();
    }
}
