package xyz.immortius.chunkbychunk.common.blockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.common.blocks.TriggeredSpawnRandomChunkBlock;
import xyz.immortius.chunkbychunk.interop.Services;

/**
 * Spawns a random chunk.
 */
public class TriggeredSpawnRandomChunkBlockEntity extends AbstractSpawnChunkBlockEntity {
    public TriggeredSpawnRandomChunkBlockEntity(BlockPos pos, BlockState state) {
        super(Services.PLATFORM.triggeredSpawnRandomChunkEntity(), pos, state, TriggeredSpawnRandomChunkBlock::getSourceChunk);
    }
}
