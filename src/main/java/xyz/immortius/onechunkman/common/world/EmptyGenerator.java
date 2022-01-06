package xyz.immortius.onechunkman.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EmptyGenerator extends ChunkGenerator {

    public static final Codec<EmptyGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(x -> x.biomes)).apply(instance, instance.stable(EmptyGenerator::new)));

    private final Registry<Biome> biomes;

    public EmptyGenerator(Registry<Biome> registry) {
        super(new FixedBiomeSource(registry.getOrThrow(Biomes.FOREST)), new StructureSettings(false));
        this.biomes = registry;
    }

    public Registry<Biome> getBiomes() {
        return biomes;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return EmptyGenerator.CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public Climate.Sampler climateSampler() {
        return (x, y, z) -> {
            return Climate.target(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        };
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, BiomeManager biomeManager, StructureFeatureManager structureFeatureManager, ChunkAccess access, GenerationStep.Carving carving) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureFeatureManager structureFeatureManager, ChunkAccess access) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel p_187712_, ChunkAccess p_187713_, StructureFeatureManager p_187714_) {
    }

    @Override
    public void createStructures(RegistryAccess p_62200_, StructureFeatureManager p_62201_, ChunkAccess p_62202_, StructureManager p_62203_, long p_62204_) {
    }

    @Override
    public void createReferences(WorldGenLevel p_62178_, StructureFeatureManager p_62179_, ChunkAccess p_62180_) {

    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess access) {
        return CompletableFuture.completedFuture(access);
    }

    @Override
    public int getSeaLevel() {
        return -65;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getBaseHeight(int p_156153_, int p_156154_, Heightmap.Types p_156155_, LevelHeightAccessor p_156156_) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int p_156150_, int p_156151_, LevelHeightAccessor p_156152_) {
        BlockState[] result = new BlockState[256];
        Arrays.fill(result, Blocks.AIR.defaultBlockState());
        return new NoiseColumn(256, result);
    }
}
