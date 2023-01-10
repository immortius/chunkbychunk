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

    @Name("start_in_village")
    @Comment("Should the initial spawn be in a village")
    private boolean startInVillage = true;

    @Name("starting_biome")
    @Comment("The tag or name of the biome the starting spawn should be in (if not a village, blank for any")
    private String startingBiome = "#minecraft:is_forest";

    public boolean isBlockPlacementAllowedOutsideSpawnedChunks() {
        return blockPlacementAllowedOutsideSpawnedChunks;
    }

    public void setBlockPlacementAllowedOutsideSpawnedChunks(boolean blockPlacementAllowedOutsideSpawnedChunks) {
        this.blockPlacementAllowedOutsideSpawnedChunks = blockPlacementAllowedOutsideSpawnedChunks;
    }

    public boolean getStartInVillage() {
        return startInVillage;
    }

    public void setStartInVillage(boolean startInVillage) {
        this.startInVillage = startInVillage;
    }

    public String getStartingBiome() {
        return startingBiome;
    }

    public void setStartingBiome(String startingBiome) {
        this.startingBiome = startingBiome;
    }
}
