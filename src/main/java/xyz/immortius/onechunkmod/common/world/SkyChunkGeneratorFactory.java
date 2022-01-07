package xyz.immortius.onechunkmod.common.world;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.common.world.ForgeWorldPreset;

/**
 * Factory for generating a SkyChunk world
 */
public class SkyChunkGeneratorFactory implements ForgeWorldPreset.IBasicChunkGeneratorFactory {

    @Override
    public ChunkGenerator createChunkGenerator(RegistryAccess registryAccess, long seed) {
        NoiseGeneratorSettings noiseSettings = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).get(NoiseGeneratorSettings.OVERWORLD);
        return new SkyChunkPrimeGenerator(new NoiseBasedChunkGenerator(registryAccess.registryOrThrow(Registry.NOISE_REGISTRY), MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(registryAccess.registryOrThrow(Registry.BIOME_REGISTRY)), seed, () -> noiseSettings));
    }

    @Override
    public WorldGenSettings createSettings(RegistryAccess dynamicRegistries, long seed, boolean generateStructures, boolean bonusChest, String generatorSettings) {
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        return new WorldGenSettings(seed, generateStructures, bonusChest,
                WorldGenSettings.withOverworld(dimensionTypeRegistry,
                        DimensionType.defaultDimensions(dynamicRegistries, seed),
                        createChunkGenerator(dynamicRegistries, seed, generatorSettings)));
    }
}
