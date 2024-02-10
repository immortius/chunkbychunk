package xyz.immortius.chunkbychunk.server.world;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import xyz.immortius.chunkbychunk.mixins.ChunkGeneratorAccessor;
import xyz.immortius.chunkbychunk.mixins.NoiseBasedChunkGeneratorMixin;

import java.util.List;

public final class ChunkGeneratorAccess {

    private ChunkGeneratorAccess() {

    }

    public static Holder<NoiseGeneratorSettings> getNoiseGeneratorSettings(ChunkGenerator generator) {
        if (generator instanceof NoiseBasedChunkGeneratorMixin noiseParent) {
            return noiseParent.getSettings();
        } else {
            return NoiseGeneratorSettings.bootstrap();
        }
    }

    public static long getSeed(ChunkGenerator generator) {
        if (generator instanceof NoiseBasedChunkGeneratorMixin noiseParent) {
            return noiseParent.getSeed();
        } else {
            return 0L;
        }
    }

    public static Registry<NormalNoise.NoiseParameters> getNoiseParamsRegistry(ChunkGenerator generator) {
        if (generator instanceof NoiseBasedChunkGeneratorMixin noiseParent) {
            return noiseParent.getNoises();
        } else {
            return BuiltinRegistries.NOISE;
        }
    }

    public static List<StructurePlacement> getPlacementsForFeature(ChunkGenerator generator, Holder<ConfiguredStructureFeature<?,?>> structure) {
        return ((ChunkGeneratorAccessor) (Object) generator).callGetPlacementsForFeature(structure);
    }
}
