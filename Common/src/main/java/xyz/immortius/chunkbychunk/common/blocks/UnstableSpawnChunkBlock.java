package xyz.immortius.chunkbychunk.common.blocks;

import xyz.immortius.chunkbychunk.interop.Services;

/**
 * Spawns a random chunk
 */
public class UnstableSpawnChunkBlock extends BaseSpawnChunkBlock {
    public UnstableSpawnChunkBlock(Properties blockProperties) {
        super(Services.PLATFORM.triggeredSpawnRandomChunkBlock().defaultBlockState(), blockProperties);
    }
}
