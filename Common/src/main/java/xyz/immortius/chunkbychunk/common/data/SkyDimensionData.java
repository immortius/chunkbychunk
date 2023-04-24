package xyz.immortius.chunkbychunk.common.data;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkyDimensionData {

    /** Id of the dimension to turn into a sky dimension */
    public String dimensionId;
    /** Optional. Id to use for the generation dimension that will be created. */
    public String genDimensionId = "";
    /** Is this sky dimension config enabled */
    public boolean enabled = true;
    /** Is the chunk spawner block usable in this dimension */
    public boolean allowChunkSpawner = true;
    /** Is the unstable chunk spawner block usable in this dimension */
    public boolean allowUnstableChunkSpawner = true;
    /** Type of generation */
    public SkyChunkGenerator.EmptyGenerationType generationType = SkyChunkGenerator.EmptyGenerationType.Normal;
    /** Block to seal chunks with for sealed generation */
    public String sealBlock = "minecraft:bedrock";
    /** The number of chunks to be spawned initally */
    public int initialChunks = 1;
    /** Configuration for dimensions that will trigger chunk spawns in this dimension */
    public List<String> synchToDimensions = new ArrayList<>();

    public String biomeThemeDimensionType;

    public Map<String, List<String>> biomeThemes = new LinkedHashMap<>();

    public boolean validate(ResourceLocation dataId, MappedRegistry<LevelStem> dimensions) {
        if (!dimensions.containsKey(new ResourceLocation(dimensionId))) {
            ChunkByChunkConstants.LOGGER.error("Invalid dimension '{}' for sky dimension {}", dimensionId, dataId);
            return false;
        }
        if (synchToDimensions == null) {
            ChunkByChunkConstants.LOGGER.error("Invalid synchDimensions array for sky dimension {}", dataId);
            return false;
        }
        for (String dim : synchToDimensions) {
            if (!dimensions.containsKey(new ResourceLocation(dim))) {
                ChunkByChunkConstants.LOGGER.error("Invalid dimension '{}' for sky dimension {}", dim, dataId);
                return false;
            }
        }
        return true;
    }

    public ResourceLocation getGenDimensionId() {
        if (genDimensionId == null) {
            return new ResourceLocation(dimensionId + "_gen");
        } else {
            return new ResourceLocation(genDimensionId);
        }
    }
}
