package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.interop.Services;

/**
 * Spawns a random chunk
 */
public class UnstableSpawnChunkBlock extends BaseSpawnChunkBlock {

    public UnstableSpawnChunkBlock(Properties blockProperties) {
        super(blockProperties);
    }

    @Override
    public BlockState getTriggeredBlockState() {
        return Services.PLATFORM.triggeredSpawnRandomChunkBlock().defaultBlockState();
    }
}
