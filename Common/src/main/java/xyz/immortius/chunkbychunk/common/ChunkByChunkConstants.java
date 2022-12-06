package xyz.immortius.chunkbychunk.common;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Constants for ChunkByChunk - may vary by mod system
 */
public final class ChunkByChunkConstants {



    private ChunkByChunkConstants() {
    }

    public static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);

    public static final String MOD_ID = "chunkbychunk";

    public static final String DEFAULT_CONFIG_PATH = "defaultconfigs";
    public static final String CONFIG_FILE = MOD_ID + ".toml";

    public static final String SCANNER_DATA_PATH = "scanner_data";
    public static final String SKY_DIMENSION_DATA_PATH = "skydimensions";

    public static final String BIOME_CHUNK_GENERATION_LEVEL_SUFFIX = "biomechunkgeneration";
    public static final String BIOME_CHUNK_BLOCK_SUFFIX = "chunkspawner";
    public static final String TRIGGERED_BIOME_CHUNK_BLOCK_SUFFIX = "triggeredchunkspawner";
    public static final String BIOME_CHUNK_BLOCK_ITEM_SUFFIX = "chunkspawner";

    public static final Set<String> BIOME_THEMES = new LinkedHashSet<>(Lists.newArrayList(
            "plains",
            "snow",
            "desert",
            "swamp",
            "badlands",
            "forest",
            "savanna",
            "rocky",
            "jungle",
            "mushroom"
    ));
}