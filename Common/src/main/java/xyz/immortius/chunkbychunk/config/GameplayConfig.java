package xyz.immortius.chunkbychunk.config;

import xyz.immortius.chunkbychunk.config.system.Comment;
import xyz.immortius.chunkbychunk.config.system.Name;

/**
 * Configuration for general gameplay
 */
public class GameplayConfig {

    @Name("block_placement_allowed_outside_spawned_chunks")
    @Comment("Can blocks be placed outside spawned chunks")
    private boolean blockPlacementAllowedOutsideSpawnedChunks = true;

    @Name("start_restriction")
    @Comment("What restriction to place on starting location")
    private StartRestriction startRestriction = StartRestriction.Village;

    @Name("starting_biome")
    @Comment("The tag or name of the biome the starting spawn should be in (if not a village, blank for any")
    private String startingBiome = "#minecraft:is_forest";

    @Name("chunk_spawn_leaf_decay_disabled")
    @Comment("Prevent leaves spawned by the chunk spawners from decaying")
    private boolean chunkSpawnLeafDecayDisabled = false;

    public boolean isBlockPlacementAllowedOutsideSpawnedChunks() {
        return blockPlacementAllowedOutsideSpawnedChunks;
    }

    public void setBlockPlacementAllowedOutsideSpawnedChunks(boolean blockPlacementAllowedOutsideSpawnedChunks) {
        this.blockPlacementAllowedOutsideSpawnedChunks = blockPlacementAllowedOutsideSpawnedChunks;
    }

    public StartRestriction getStartRestriction() {
        return startRestriction;
    }

    public void setStartRestriction(StartRestriction startRestriction) {
        this.startRestriction = startRestriction;
    }

    public String getStartingBiome() {
        return startingBiome;
    }

    public void setStartingBiome(String startingBiome) {
        this.startingBiome = startingBiome;
    }

    public boolean isChunkSpawnLeafDecayDisabled() {
        return chunkSpawnLeafDecayDisabled;
    }

    public void setChunkSpawnLeafDecayDisabled(boolean chunkSpawnLeafDecayDisabled) {
        this.chunkSpawnLeafDecayDisabled = chunkSpawnLeafDecayDisabled;
    }

    public enum StartRestriction {
        None,
        Village,
        Biome
    }
}
