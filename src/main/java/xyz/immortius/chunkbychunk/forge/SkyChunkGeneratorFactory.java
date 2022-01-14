package xyz.immortius.chunkbychunk.forge;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.common.world.ForgeWorldPreset;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.forge.ChunkByChunkMod;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

/**
 * Factory for generating a SkyChunk world. Will add an extra dimension for generating the true world to copy into the
 * overworld.
 */
public class SkyChunkGeneratorFactory implements ForgeWorldPreset.IBasicChunkGeneratorFactory {

    private static final ResourceKey<LevelStem> SKY_CHUNK_GENERATION_LEVEL_STEM = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(ChunkByChunkConstants.MOD_ID, "skychunkgeneration"));

    private final boolean generateSealedWorld;

    public SkyChunkGeneratorFactory(boolean generateSealedWorld) {
        this.generateSealedWorld = generateSealedWorld;
    }

    @Override
    public ChunkGenerator createChunkGenerator(RegistryAccess registryAccess, long seed) {
        return new SkyChunkGenerator(WorldGenSettings.makeDefaultOverworld(registryAccess, seed), generateSealedWorld);
    }

    @Override
    public WorldGenSettings createSettings(RegistryAccess dynamicRegistries, long seed, boolean generateStructures, boolean bonusChest, String generatorSettings) {
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        MappedRegistry<LevelStem> levelStems = WorldGenSettings.withOverworld(dimensionTypeRegistry,
                DimensionType.defaultDimensions(dynamicRegistries, seed),
                createChunkGenerator(dynamicRegistries, seed, generatorSettings));

        if (levelStems.get(LevelStem.OVERWORLD).generator() instanceof SkyChunkGenerator primeGenerator) {
            levelStems.register(SKY_CHUNK_GENERATION_LEVEL_STEM, new LevelStem(() -> dimensionTypeRegistry.get(DimensionType.OVERWORLD_LOCATION), primeGenerator.getParent()), Lifecycle.stable());
        }
        return new WorldGenSettings(seed, generateStructures, bonusChest, levelStems);
    }
}
