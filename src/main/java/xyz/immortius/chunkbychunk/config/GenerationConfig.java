package xyz.immortius.chunkbychunk.config;

import xyz.immortius.chunkbychunk.config.system.Comment;
import xyz.immortius.chunkbychunk.config.system.IntRange;
import xyz.immortius.chunkbychunk.config.system.Name;

/**
 * Chunk Generation configuration
 */
public class GenerationConfig {
    @Name("seal_world")
    @Comment("Should empty chunks be generated as bedrock")
    private boolean sealWorld = false;

    @Name("spawn_new_chunk_chest")
    @Comment("Should chunks include a chest with materials for generating further chunks?")
    private boolean spawnNewChunkChest = true;

    @Name("use_bedrock_chest")
    @Comment("Should the generated chest be a bedrock chest")
    private boolean useBedrockChest = false;

    @Name("min_chest_spawn_depth")
    @Comment("The minimum depth at which the chunk spawner chest can spawn")
    @IntRange(min = -64, max = 128)
    private int minChestSpawnDepth = -60;

    @Name("max_chest_spawn_depth")
    @Comment("The maximum depth at which the chunk spawner chest can spawn")
    @IntRange(min = -64, max = 128)
    private int maxChestSpawnDepth = -60;

    @Name("chest_contents")
    @Comment("The type of items the bedrock chest provides")
    private ChunkRewardChestContent chestContents = ChunkRewardChestContent.WorldCore;

    @Name("chest_quantity")
    @Comment("The number of items the bedrock chest provides")
    @IntRange(min = 1, max = 64)
    private int chestQuantity = 1;

    @Name("initial_chunks")
    @Comment("The number of chunks to spawn initially (up to 9).")
    @IntRange(min = 1, max = 9)
    private int initialChunks = 1;

    @Name("chunk_gen_x_offset")
    @Comment("Offsets the spawn of chunk from the standard generator. e.g. an offset of 3 means the (0,0) chunk will be the (3,0) chunk of the world")
    @IntRange(min = Short.MIN_VALUE, max = Short.MAX_VALUE)
    private int chunkGenXOffset = 0;

    @Name("chunk_gen_z_offset")
    @Comment("Offsets the spawn of chunk from the standard generator.")
    @IntRange(min = Short.MIN_VALUE, max = Short.MAX_VALUE)
    private int chunkGenZOffset = 0;

    public boolean useBedrockChest() { return useBedrockChest; }

    public int getInitialChunks() {
        return initialChunks;
    }

    public int getChunkGenXOffset() {
        return chunkGenXOffset;
    }

    public int getChunkGenZOffset() {
        return chunkGenZOffset;
    }

    public boolean spawnNewChunkChest() {
        return spawnNewChunkChest;
    }

    public int getChestQuantity() {
        return chestQuantity;
    }

    public ChunkRewardChestContent getChestContents() {
        return chestContents;
    }

    public int getMinChestSpawnDepth() {
        return minChestSpawnDepth;
    }

    public int getMaxChestSpawnDepth() {
        return maxChestSpawnDepth;
    }

    public boolean sealWorld() {
        return sealWorld;
    }
}
