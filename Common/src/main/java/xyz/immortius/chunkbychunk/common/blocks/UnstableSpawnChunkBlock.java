package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.server.level.ServerLevel;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.interop.Services;

/**
 * Spawns a random chunk
 */
public class UnstableSpawnChunkBlock extends BaseSpawnChunkBlock {
    public UnstableSpawnChunkBlock(Properties blockProperties) {
        super(Services.PLATFORM.triggeredSpawnRandomChunkBlock().defaultBlockState(), blockProperties);
    }

    @Override
    public boolean isValidForLevel(ServerLevel level) {
        return level.getChunkSource().getGenerator() instanceof SkyChunkGenerator generator && generator.isRandomChunkSpawnerAllowed();
    }
}
