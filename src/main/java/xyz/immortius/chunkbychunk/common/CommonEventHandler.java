package xyz.immortius.chunkbychunk.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkSettings;

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
        return ChunkByChunkSettings.isBlockPlacementAllowedOutsideSpawnedChunks() ||
                !playerEntity.getLevel().dimension().equals(Level.OVERWORLD) ||
                !SpawnChunkHelper.isEmptyChunk(level, new ChunkPos(pos));
    }
}
