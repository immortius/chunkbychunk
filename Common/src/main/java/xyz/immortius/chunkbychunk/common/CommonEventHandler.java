package xyz.immortius.chunkbychunk.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.server.world.SpawnChunkHelper;

/**
 * Event handlers used across both server and client.
 */
public final class CommonEventHandler {
    private CommonEventHandler() {

    }

    /**
     * This method determines whether block placement is allowed. It is called when a block is placed on both server
     * and client
     * @param pos The block position where placement is being attempted.
     * @param playerEntity The player placing the block
     * @param level The level the block is being placed in
     * @return Whether the placement is allowed
     */
    public static boolean isBlockPlacementAllowed(BlockPos pos, Entity playerEntity, LevelAccessor level) {
        return ChunkByChunkConfig.get().getGameplayConfig().isBlockPlacementAllowedOutsideSpawnedChunks() ||
                !playerEntity.level().dimension().equals(Level.OVERWORLD) ||
                !SpawnChunkHelper.isEmptyChunk(level, new ChunkPos(pos));
    }
}
