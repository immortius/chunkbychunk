package xyz.immortius.chunkbychunk.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkSettings;

public final class CommonEventHandler {
    private CommonEventHandler() {

    }

    public static boolean isBlockPlacementAllowed(BlockPos pos, Entity playerEntity, LevelAccessor level) {
        return ChunkByChunkSettings.isBlockPlacementAllowedOutsideSpawnedChunks() ||
                !playerEntity.getLevel().dimension().equals(Level.OVERWORLD) ||
                !SpawnChunkHelper.isEmptyChunk(level, new ChunkPos(pos));
    }
}
