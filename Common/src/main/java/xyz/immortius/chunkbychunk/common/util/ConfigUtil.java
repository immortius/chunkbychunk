package xyz.immortius.chunkbychunk.common.util;

import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.config.system.ConfigSystem;

import java.nio.file.Paths;

/**
 * Utility methods for working with Config
 */
public final class ConfigUtil {
    private ConfigUtil() {

    }

    private static final ConfigSystem system = new ConfigSystem();

    /**
     * Loads the default (not world specific) config.
     */
    public static void loadDefaultConfig() {
        synchronized (system) {
            system.synchConfig(Paths.get(ChunkByChunkConstants.DEFAULT_CONFIG_PATH).resolve(ChunkByChunkConstants.CONFIG_FILE), ChunkByChunkConfig.get());
        }
    }

    /**
     * Saves the default (not world specific) config.
     */
    public static void saveDefaultConfig() {
        synchronized (system) {
            system.write(Paths.get(ChunkByChunkConstants.DEFAULT_CONFIG_PATH).resolve(ChunkByChunkConstants.CONFIG_FILE), ChunkByChunkConfig.get());
        }
    }
}
