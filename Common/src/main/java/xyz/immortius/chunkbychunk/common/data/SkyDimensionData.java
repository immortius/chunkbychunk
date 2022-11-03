package xyz.immortius.chunkbychunk.common.data;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;

import java.util.ArrayList;
import java.util.List;

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
    /** Are biome theme spawner blocks usable in this dimension */
    public boolean allowBiomeSpawners = false;
    /** Type of generation */
    public SkyChunkGenerator.EmptyGenerationType generationType = SkyChunkGenerator.EmptyGenerationType.Normal;
    /** The number of chunks to be spawned initally */
    public int initialChunks = 1;
    /** Configuration for dimensions that will trigger chunk spawns in this dimension */
    public List<SynchDimension> synchDimensions = new ArrayList<>();

    public static class SynchDimension {
        /** The id of the dimension to synchronize spawns with */
        public String dimensionId;
        /** The scaling of the synchronization. */
        public int scale;
    }

    public boolean validate(ResourceLocation dataId, MappedRegistry<LevelStem> dimensions) {
        if (!dimensions.containsKey(new ResourceLocation(dimensionId))) {
            ChunkByChunkConstants.LOGGER.error("Invalid dimension '{}' for sky dimension {}", dimensionId, dataId);
            return false;
        }
        if (synchDimensions == null) {
            ChunkByChunkConstants.LOGGER.error("Invalid synchDimensions array for sky dimension {}", dataId);
            return false;
        }
        for (SynchDimension dim : synchDimensions) {
            if (!dimensions.containsKey(new ResourceLocation(dim.dimensionId))) {
                ChunkByChunkConstants.LOGGER.error("Invalid dimension '{}' for sky dimension {}", dimensionId, dataId);
                return false;
            }
        }
        return true;
    }


}
