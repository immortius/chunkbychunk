package xyz.immortius.chunkbychunk.interop;

import xyz.immortius.chunkbychunk.forge.ChunkByChunkConfig;

/**
 * Access to ChunkByChunk settings
 */
public class ChunkByChunkSettings {

    public static boolean spawnNewChunkChest() {
        return ChunkByChunkConfig.spawnNewChunkChest.get();
    }

    public static int minChestSpawnDepth() {
        return ChunkByChunkConfig.minChestSpawnDepth();
    }

    public static int maxChestSpawnDepth() {
        return ChunkByChunkConfig.maxChestSpawnDepth();
    }

    public static int initialChunks() {
        return ChunkByChunkConfig.initialChunks.get();
    }

    public static int numChunkSpawners() {
        return ChunkByChunkConfig.numChunkSpawners.get();
    }

    public static boolean giveUnstableChunkSpawners() {
        return ChunkByChunkConfig.giveUnstableChunkSpawners.get();
    }

    public static int bedrockChestBlocksRemainingThreshold() {
        return ChunkByChunkConfig.bedrockChestBlocksRemainingThreshold.get();
    }

    public static int chunkGenXOffset() {
        return ChunkByChunkConfig.chunkGenXOffset.get();
    }

    public static int chunkGenZOffset() {
        return ChunkByChunkConfig.chunkGenZOffset.get();
    }

}
