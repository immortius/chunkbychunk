package xyz.immortius.chunkbychunk.server.world;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import xyz.immortius.chunkbychunk.mixins.ChunkGeneratorAccessor;
import xyz.immortius.chunkbychunk.mixins.NoiseBasedChunkGeneratorMixin;

import java.util.List;

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

    public static Registry<NormalNoise.NoiseParameters> getNoiseParamsRegistry(ChunkGenerator generator) {
        if (generator instanceof NoiseBasedChunkGenerator noiseParent) {
            return ((NoiseBasedChunkGeneratorMixin) noiseParent).getNoises();
        } else {
            return BuiltinRegistries.NOISE;
        }
    }

    public static List<StructurePlacement> getPlacementsForStructure(ChunkGenerator generator, Holder<Structure> structure, RandomState state) {
        return ((ChunkGeneratorAccessor) (Object) generator).callGetPlacementsForStructure(structure, state);
    }
}
