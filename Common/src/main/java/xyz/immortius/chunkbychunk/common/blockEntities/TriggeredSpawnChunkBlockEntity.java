package xyz.immortius.chunkbychunk.common.blockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.common.blocks.TriggeredSpawnChunkBlock;
import xyz.immortius.chunkbychunk.interop.Services;

/**
 * Spawns the corresponding chunk for the chunk the block entity is in.
 */
public class TriggeredSpawnChunkBlockEntity extends AbstractSpawnChunkBlockEntity {
    public TriggeredSpawnChunkBlockEntity(BlockPos pos, BlockState state) {
        super(Services.PLATFORM.triggeredSpawnChunkEntity(), pos, state, TriggeredSpawnChunkBlock::getSourceChunk);
    }
}
