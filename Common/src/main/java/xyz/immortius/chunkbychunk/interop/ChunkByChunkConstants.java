package xyz.immortius.chunkbychunk.interop;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static final ResourceKey<Level> SKY_CHUNK_GENERATION_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "skychunkgeneration"));
    public static final ResourceKey<Level> NETHER_CHUNK_GENERATION_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "netherchunkgeneration"));
}
