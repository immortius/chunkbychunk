package xyz.immortius.chunkbychunk.common.world;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.data.SkyDimensionData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public final class SkyDimensions {

    private SkyDimensions() {
    }

    private static final Map<ResourceLocation, SkyDimensionData> skyDimensions = new LinkedHashMap<>();

    public static void loadSkyDimensionData(ResourceManager resourceManager, Gson gson) {
        int count = 0;
        skyDimensions.clear();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(ChunkByChunkConstants.SKY_DIMENSION_DATA_PATH, r -> true).entrySet()) {
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {
                SkyDimensionData data = gson.fromJson(reader, SkyDimensionData.class);
                skyDimensions.put(entry.getKey(), data);
                count++;
            } catch (IOException |RuntimeException e) {
                ChunkByChunkConstants.LOGGER.error("Failed to read sky dimension data '{}'", entry.getKey(), e);
            }
        }
        ChunkByChunkConstants.LOGGER.info("Loaded {} sky dimensions", count);
    }

    public static Map<ResourceLocation, SkyDimensionData> getSkyDimensions() {
        return Collections.unmodifiableMap(skyDimensions);
    }
}
