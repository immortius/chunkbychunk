package xyz.immortius.chunkbychunk.common.world;

import net.minecraft.world.level.ChunkPos;

public interface ControllableChunkMap {

    void forceReloadChunk(ChunkPos chunk);
}
