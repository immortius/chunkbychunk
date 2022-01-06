package xyz.immortius.onechunkman.common.world;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.common.world.ForgeWorldPreset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory for generating a OneChunkGeneratort - this is a noise generator but wrapped to produce only a single chunk by default
 */
public class OneChunkGeneratorFactory implements ForgeWorldPreset.IBasicChunkGeneratorFactory {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public ChunkGenerator createChunkGenerator(RegistryAccess registryAccess, long seed) {
        //Registry.register(Registry.DIMENSION_TYPE_REGISTRY, "onechunk", new DimensionType());
        registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).entrySet().forEach(x -> {
          LOGGER.info("{}", x.getKey());
        });

        NoiseGeneratorSettings noiseSettings = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).get(new ResourceLocation("minecraft", "overworld"));
        return new OneChunkGenerator(new NoiseBasedChunkGenerator(registryAccess.registryOrThrow(Registry.NOISE_REGISTRY), MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(registryAccess.registryOrThrow(Registry.BIOME_REGISTRY)), seed, () -> noiseSettings));
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
