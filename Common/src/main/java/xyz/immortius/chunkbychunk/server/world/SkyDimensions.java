package xyz.immortius.chunkbychunk.server.world;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.data.SkyDimensionData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SkyDimensions {

    private SkyDimensions() {
    }

    private static final Map<ResourceLocation, SkyDimensionData> skyDimensions = new LinkedHashMap<>();

    public static void loadSkyDimensionData(ResourceManager resourceManager, Gson gson) {
        int count = 0;
        skyDimensions.clear();
        for (ResourceLocation location : resourceManager.listResources(ChunkByChunkConstants.SKY_DIMENSION_DATA_PATH, r -> !r.isEmpty() && !ChunkByChunkConstants.SKY_DIMENSION_DATA_PATH.equals(r))) {
            try (InputStreamReader reader = new InputStreamReader(resourceManager.getResource(location).getInputStream())) {
                SkyDimensionData data = gson.fromJson(reader, SkyDimensionData.class);
                skyDimensions.put(location, data);
                count++;
            } catch (IOException |RuntimeException e) {
                ChunkByChunkConstants.LOGGER.error("Failed to read sky dimension data '{}'", location, e);
            }
        }
        ChunkByChunkConstants.LOGGER.info("Loaded {} sky dimensions", count);
    }

    public static Map<ResourceLocation, SkyDimensionData> getSkyDimensions() {
        return Collections.unmodifiableMap(skyDimensions);
    }
}
