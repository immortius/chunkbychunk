package xyz.immortius.chunkbychunk.common;

import com.google.common.collect.Lists;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

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

    public static final ResourceKey<Level> SKY_CHUNK_GENERATION_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "skychunkgeneration"));
    public static final ResourceKey<Level> NETHER_CHUNK_GENERATION_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "netherchunkgeneration"));

    public static final String BIOME_CHUNK_GENERATION_LEVEL_SUFFIX = "biomechunkgeneration";
    public static final String BIOME_CHUNK_BlOCK_SUFFIX = "chunkspawner";
    public static final String TRIGGERED_BIOME_CHUNK_BLOCK_SUFFIX = "triggeredchunkspawner";
    public static final String BIOME_CHUNK_BlOCK_ITEM_SUFFIX = "chunkspawner";

    public record BiomeGroup(String name, ResourceKey<Biome>... biomes) {
        @SafeVarargs
        public BiomeGroup {
        }
    }

    public static final List<BiomeGroup> OVERWORLD_BIOME_SPAWNERS = new ArrayList<>(Lists.newArrayList(
            new BiomeGroup("plains", Biomes.PLAINS, Biomes.MEADOW, Biomes.SUNFLOWER_PLAINS),
            new BiomeGroup("snow", Biomes.SNOWY_PLAINS, Biomes.SNOWY_BEACH, Biomes.SNOWY_SLOPES, Biomes.GROVE, Biomes.SNOWY_TAIGA, Biomes.ICE_SPIKES, Biomes.FROZEN_RIVER, Biomes.FROZEN_PEAKS),
            new BiomeGroup("desert", Biomes.DESERT),
            new BiomeGroup("swamp", Biomes.SWAMP, Biomes.MANGROVE_SWAMP),
            new BiomeGroup("badlands", Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS),
            new BiomeGroup("forest", Biomes.FOREST, Biomes.DARK_FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.TAIGA),
            new BiomeGroup("savanna", Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU),
            new BiomeGroup("rocky", Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_SAVANNA ,Biomes.JAGGED_PEAKS, Biomes.STONY_PEAKS, Biomes.STONY_SHORE),
            new BiomeGroup("jungle", Biomes.SPARSE_JUNGLE, Biomes.JUNGLE, Biomes.BAMBOO_JUNGLE),
            new BiomeGroup("mushroom", Biomes.MUSHROOM_FIELDS)
    ));
}