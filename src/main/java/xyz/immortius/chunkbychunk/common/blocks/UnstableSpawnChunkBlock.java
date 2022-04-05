package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

/**
 * Spawns a random chunk
 */
public class UnstableSpawnChunkBlock extends BaseSpawnChunkBlock {

    public UnstableSpawnChunkBlock(Properties blockProperties) {
        super(blockProperties);
    }

    @Override
    public BlockState getTriggeredBlockState() {
        return ChunkByChunkConstants.triggeredSpawnRandomChunkBlock().defaultBlockState();
    }
}
