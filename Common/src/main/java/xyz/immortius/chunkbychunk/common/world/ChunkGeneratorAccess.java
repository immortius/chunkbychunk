package xyz.immortius.chunkbychunk.common.world;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import xyz.immortius.chunkbychunk.mixins.NoiseBasedChunkGeneratorMixin;

public final class ChunkGeneratorAccess {
    private ChunkGeneratorAccess() {

    }

    public static Holder<NoiseGeneratorSettings> getNoiseGeneratorSettings(ChunkGenerator generator) {
        if (generator instanceof NoiseBasedChunkGenerator noiseParent) {
            return noiseParent.generatorSettings();
        } else {
            return new Holder.Direct<>(NoiseGeneratorSettings.dummy());
        }
    }

    public static Registry<NormalNoise.NoiseParameters> getNoiseParamsRegistry(ChunkGenerator parent) {
        if (parent instanceof NoiseBasedChunkGenerator noiseParent) {
            return ((NoiseBasedChunkGeneratorMixin) noiseParent).getNoises();
        } else {
            return BuiltinRegistries.NOISE;
        }
    }
}
