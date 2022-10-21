package xyz.immortius.chunkbychunk.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import xyz.immortius.chunkbychunk.mixins.ChunkGeneratorStructureAccessor;
import xyz.immortius.chunkbychunk.mixins.NoiseBasedChunkGeneratorMixin;

import java.util.Arrays;

public class FixedBiomeChunkGenerator extends NoiseBasedChunkGenerator {

    private static Holder<NoiseGeneratorSettings> getNoiseGeneratorSettings(ChunkGenerator generator) {
        if (generator instanceof NoiseBasedChunkGenerator noiseParent) {
            return noiseParent.generatorSettings();
        } else {
            return new Holder.Direct<>(NoiseGeneratorSettings.dummy());
        }
    }

    private static Registry<NormalNoise.NoiseParameters> getNoiseParamsRegistry(ChunkGenerator parent) {
        if (parent instanceof NoiseBasedChunkGenerator noiseParent) {
            return ((NoiseBasedChunkGeneratorMixin) noiseParent).getNoises();
        } else {
            return BuiltinRegistries.NOISE;
        }
    }

    /**
     * @param parent The chunkGenerator this generator is based on
     */
    public FixedBiomeChunkGenerator(ChunkGenerator parent, Holder<Biome> biome) {
        super(((ChunkGeneratorStructureAccessor) parent).getStructureSet(), getNoiseParamsRegistry(parent), new FixedBiomeSource(biome), getNoiseGeneratorSettings(parent));
    }

}
