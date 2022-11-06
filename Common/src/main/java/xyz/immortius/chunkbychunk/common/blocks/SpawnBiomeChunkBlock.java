package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;

/**
 * Spawns a chunk from the equivalent chunk in the source dimension (with configuration offset)
 */
public class SpawnBiomeChunkBlock extends BaseSpawnChunkBlock {

    private final String biomeTheme;

    public SpawnBiomeChunkBlock(String biomeTheme, Block triggeredSpawnChunkBlock, Properties blockProperties) {
        super(triggeredSpawnChunkBlock.defaultBlockState(), blockProperties);
        this.biomeTheme = biomeTheme;
    }

    @Override
    public boolean isValidForLevel(ServerLevel level) {
        return level.getChunkSource().getGenerator() instanceof SkyChunkGenerator generator && generator.getBiomeDimension(biomeTheme) != null;
    }
}
