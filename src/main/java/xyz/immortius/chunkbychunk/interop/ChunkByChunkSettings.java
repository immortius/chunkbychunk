package xyz.immortius.chunkbychunk.interop;

import xyz.immortius.chunkbychunk.config.BedrockChestContents;
import xyz.immortius.chunkbychunk.forge.ChunkByChunkConfig;

/**
 * Access to ChunkByChunk settings
 */
public class ChunkByChunkSettings {

    public static boolean sealWorld() { return ChunkByChunkConfig.sealWorld.get(); }

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

    public static int chestQuantity() {
        return ChunkByChunkConfig.chestQuantity.get();
    }

    public static BedrockChestContents chestContents() {
        return ChunkByChunkConfig.chestContents.get();
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

    public static int worldForgeProductionRate() {
        return ChunkByChunkConfig.worldForgeProductionRate.get();
    }

    public static int worldForgeSoilFuelValue() {
        return ChunkByChunkConfig.worldForgeSoilFuelValue.get();
    }

    public static int worldForgeStoneFuelValue() {
        return ChunkByChunkConfig.worldForgeStoneFuelValue.get();
    }

    public static int worldForgeFuelPerFragment() {
        return ChunkByChunkConfig.worldForgeFuelPerFragment.get();
    }

    public static boolean isBlockPlacementAllowedOutsideSpawnedChunks() {
        return ChunkByChunkConfig.blockPlacementAllowedOutsideSpawnedChunks.get();
    }
}
