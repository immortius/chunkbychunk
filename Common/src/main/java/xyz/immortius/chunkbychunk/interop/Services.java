package xyz.immortius.chunkbychunk.interop;

import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;

import java.util.ServiceLoader;

public class Services {

    public static final CBCPlatformHelper PLATFORM = load(CBCPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        ChunkByChunkConstants.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
