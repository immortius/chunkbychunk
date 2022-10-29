package xyz.immortius.chunkbychunk.common.world;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import xyz.immortius.chunkbychunk.mixins.ChunkGeneratorStructureAccessor;

public class FixedBiomeChunkGenerator extends NoiseBasedChunkGenerator {

    /**
     * @param parent The chunkGenerator this generator is based on
     */
    public FixedBiomeChunkGenerator(ChunkGenerator parent, Holder<Biome> biome) {
        super(((ChunkGeneratorStructureAccessor) parent).getStructureSet(), ChunkGeneratorAccess.getNoiseParamsRegistry(parent), new FixedBiomeSource(biome), ChunkGeneratorAccess.getSeed(parent), ChunkGeneratorAccess.getNoiseGeneratorSettings(parent));
    }

}
