package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.common.blockEntities.TriggeredSpawnRandomChunkBlockEntity;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.Random;

/**
 * Spawns a pseudo-random chunk from the generation dimension (position determined from block position).
 * Pseudo-random used so that the same chunk is forced, generated and unforced.
 */
public class TriggeredSpawnRandomChunkBlock extends AbstractTriggeredSpawnChunkBlock {

    public TriggeredSpawnRandomChunkBlock(Properties blockProperties) {
        super(AbstractTriggeredSpawnChunkBlock::getSkyGenerationSourceLevel, TriggeredSpawnRandomChunkBlock::getSourceChunk, blockProperties);
    }

    public static ChunkPos getSourceChunk(BlockPos targetBlockPos) {
        Random random = new Random(targetBlockPos.asLong());
        return new ChunkPos(random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE), random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TriggeredSpawnRandomChunkBlockEntity(pos, state);
    }

    @Override
    public boolean validForLevel(ServerLevel level) {
        return level.getChunkSource().getGenerator() instanceof SkyChunkGenerator;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return level.isClientSide ? null : createTickerHelper(entityType, Services.PLATFORM.triggeredSpawnRandomChunkEntity(), TriggeredSpawnRandomChunkBlockEntity::serverTick);
    }
}
