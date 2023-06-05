package xyz.immortius.chunkbychunk.config;

import xyz.immortius.chunkbychunk.config.system.Comment;
import xyz.immortius.chunkbychunk.config.system.IntRange;
import xyz.immortius.chunkbychunk.config.system.Name;

/**
 * Chunk Generation configuration
 */
public class GenerationConfig {
    @Name("enabled")
    @Comment("Is ChunkByChunk generation enabled")
    private boolean enabled = true;

    @Name("seal_world")
    @Comment("Should empty chunks be generated as bedrock")
    private boolean sealWorld = false;

    @Name("synch_nether_chunk_spawn")
    @Comment("Should the nether start empty with chunks spawning in response to overworld spawns")
    private boolean synchNether = false;

    @Name("spawn_new_chunk_chest")
    @Comment("Should chunks include a chest with materials for generating further chunks?")
    private boolean spawnNewChunkChest = true;

    @Name("spawn_chest_in_initial_chunk_only")
    @Comment("Should the chest spawn in the initial chunk only?")
    private boolean spawnChestInInitialChunkOnly = true;

    @Name("use_bedrock_chest")
    @Comment("Should the generated chest be a bedrock chest")
    private boolean useBedrockChest = false;

    @Name("chest_contents")
    @Comment("The type of items the bedrock chest provides")
    private ChunkRewardChestContent chestContents = ChunkRewardChestContent.WorldForge;

    @Name("chest_quantity")
    @Comment("The number of items the bedrock chest provides")
    @IntRange(min = 1, max = 64)
    private int chestQuantity = 1;

    @Name("min_chest_spawn_depth")
    @Comment("The minimum depth at which the chunk spawner chest can spawn")
    @IntRange(min = -64, max = 128)
    private int minChestSpawnDepth = -60;

    @Name("max_chest_spawn_depth")
    @Comment("The maximum depth at which the chunk spawner chest can spawn")
    @IntRange(min = -64, max = 128)
    private int maxChestSpawnDepth = -60;

    @Name("initial_chunks")
    @Comment("The number of chunks to spawn initially")
    @IntRange(min = 0, max = 25)
    private int initialChunks = 1;

    @Name("spawn_chunk_strip")
    @Comment("Whether to spawn a full strip of chunks along an axis")
    private boolean spawnChunkStrip = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSynchNether() {
        return synchNether;
    }

    public void setSynchNether(boolean synchNether) {
        this.synchNether = synchNether;
    }

    public boolean useBedrockChest() { return useBedrockChest; }

    public void setUseBedrockChest(boolean useBedrockChest) {
        this.useBedrockChest = useBedrockChest;
    }

    public boolean spawnChestInInitialChunkOnly() {
        return spawnChestInInitialChunkOnly;
    }

    public void setSpawnChestInInitialChunkOnly(boolean spawnChestInInitialChunkOnly) {
        this.spawnChestInInitialChunkOnly = spawnChestInInitialChunkOnly;
    }

    public int getInitialChunks() {
        return initialChunks;
    }

    public void setInitialChunks(int initialChunks) {
        this.initialChunks = initialChunks;
    }

    public boolean spawnNewChunkChest() {
        return spawnNewChunkChest;
    }

    public void setSpawnNewChunkChest(boolean spawnNewChunkChest) {
        this.spawnNewChunkChest = spawnNewChunkChest;
    }

    public int getChestQuantity() {
        return chestQuantity;
    }

    public void setChestQuantity(int chestQuantity) {
        this.chestQuantity = chestQuantity;
    }

    public ChunkRewardChestContent getChestContents() {
        return chestContents;
    }

    public void setChestContents(ChunkRewardChestContent chestContents) {
        this.chestContents = chestContents;
    }

    public int getMinChestSpawnDepth() {
        return minChestSpawnDepth;
    }

    public void setMinChestSpawnDepth(int minChestSpawnDepth) {
        this.minChestSpawnDepth = minChestSpawnDepth;
    }

    public int getMaxChestSpawnDepth() {
        return maxChestSpawnDepth;
    }

    public void setMaxChestSpawnDepth(int maxChestSpawnDepth) {
        this.maxChestSpawnDepth = maxChestSpawnDepth;
    }

    public boolean sealWorld() {
        return sealWorld;
    }

    public void setSealWorld(boolean sealWorld) {
        this.sealWorld = sealWorld;
    }

    public boolean isSpawnChunkStrip() {
        return spawnChunkStrip;
    }

    public void setSpawnChunkStrip(boolean spawnChunkStrip) {
        this.spawnChunkStrip = spawnChunkStrip;
    }
}
