package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.common.blockEntities.TriggeredSpawnChunkBlockEntity;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

/**
 * Spawns the corresponding chunk to the chunk the block is in
 */
public class TriggeredSpawnChunkBlock extends AbstractTriggeredSpawnChunkBlock {

    public TriggeredSpawnChunkBlock(Properties blockProperties) {
        super(blockProperties, TriggeredSpawnChunkBlock::getSourceChunk);
    }

    public static ChunkPos getSourceChunk(BlockPos targetBlockPos) {
        ChunkPos targetChunkPos = new ChunkPos(targetBlockPos);
        return new ChunkPos(targetChunkPos.x, targetChunkPos.z);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TriggeredSpawnChunkBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return level.isClientSide ? null : createTickerHelper(entityType, ChunkByChunkConstants.triggeredSpawnChunkEntity(), TriggeredSpawnChunkBlockEntity::serverTick);
    }
}
