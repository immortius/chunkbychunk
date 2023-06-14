package xyz.immortius.chunkbychunk.server.world;

import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

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
}
