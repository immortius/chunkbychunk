package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.common.blockEntities.TriggeredSpawnChunkBlockEntity;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.interop.Services;

/**
 * Spawns the corresponding chunk to the chunk the block is in
 */
public class TriggeredBiomeSpawnChunkBlock extends AbstractTriggeredSpawnChunkBlock {

    private final String biomeTheme;

    public TriggeredBiomeSpawnChunkBlock(String biomeTheme, Properties blockProperties) {
        super((level) -> {
            if (level.getChunkSource().getGenerator() instanceof SkyChunkGenerator generator) {
                return generator.getBiomeDimension(biomeTheme);
            }
            return null;
        }, TriggeredBiomeSpawnChunkBlock::getSourceChunk, blockProperties);
        this.biomeTheme = biomeTheme;
    }

    @Override
    public boolean validForLevel(ServerLevel level) {
        if (level.getChunkSource().getGenerator() instanceof SkyChunkGenerator generator) {
            return generator.getBiomeDimension(biomeTheme) != null;
        }
        return false;
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
        return level.isClientSide ? null : createTickerHelper(entityType, Services.PLATFORM.triggeredSpawnChunkEntity(), TriggeredSpawnChunkBlockEntity::serverTick);
    }
}
