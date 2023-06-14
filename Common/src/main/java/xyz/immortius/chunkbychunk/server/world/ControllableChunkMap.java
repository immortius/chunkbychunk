package xyz.immortius.chunkbychunk.server.world;

import net.minecraft.world.level.ChunkPos;

public interface ControllableChunkMap {

    void forceReloadChunk(ChunkPos chunk);
}
