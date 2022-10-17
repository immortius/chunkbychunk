package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.world.level.block.Block;

/**
 * Spawns a chunk from the equivalent chunk in the source dimension (with configuration offset)
 */
public class SpawnChunkBlock extends BaseSpawnChunkBlock {

    public SpawnChunkBlock(Block triggeredSpawnChunkBlock, Properties blockProperties) {
        super(triggeredSpawnChunkBlock.defaultBlockState(), blockProperties);
    }

}
