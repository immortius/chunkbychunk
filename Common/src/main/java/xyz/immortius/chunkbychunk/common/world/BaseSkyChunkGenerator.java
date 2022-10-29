package xyz.immortius.chunkbychunk.common.world;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import xyz.immortius.chunkbychunk.mixins.ChunkGeneratorStructureAccessor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * The Base Sky Chunk Generator - Sky Chunk Generators wrap a parent generator but disable actual generation. The
 * parent generator is retained for biome information and similar. Each sky chunk generator also has a reference
 * to the dimension that generates chunks for this dimension
 */
public abstract class BaseSkyChunkGenerator extends NoiseBasedChunkGenerator {

    protected final ChunkGenerator parent;
    protected final ResourceKey<Level> generationLevel;

    /**
     * @param parent The chunkGenerator this generator is based on
     */
    public BaseSkyChunkGenerator(ChunkGenerator parent, ResourceKey<Level> generationLevel) {
        super(((ChunkGeneratorStructureAccessor) parent).getStructureSet(), ChunkGeneratorAccess.getNoiseParamsRegistry(parent), parent.getBiomeSource(), ChunkGeneratorAccess.getSeed(parent), ChunkGeneratorAccess.getNoiseGeneratorSettings(parent));
        this.parent = parent;
        this.generationLevel = generationLevel;
    }

    public ChunkGenerator getParent() {
        return parent;
    }

    public ResourceKey<Level> getGenerationLevel() {
        return generationLevel;
    }

    @Override
    public Stream<Holder<StructureSet>> possibleStructureSets() {
        return parent.possibleStructureSets();
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor heightAccessor) {
        return parent.getSpawnHeight(heightAccessor);
    }

    @Override
    public BiomeSource getBiomeSource() {
        return parent.getBiomeSource();
    }

    @Override
    public int getGenDepth() {
        return parent.getGenDepth();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager featureManager, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return parent.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return parent.getMinY();
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> biomeRegistry, Executor executor, Blender blender, StructureFeatureManager featureManager, ChunkAccess chunkAccess) {
        return parent.createBiomes(biomeRegistry, executor, blender, featureManager, chunkAccess);
    }

    @Override
    public Climate.Sampler climateSampler() {
        return parent.climateSampler();
    }

    @Override
    public int getBaseHeight(int x, int y, Heightmap.Types types, LevelHeightAccessor accessor) {
        return parent.getBaseHeight(x, y, types, accessor);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int y, LevelHeightAccessor accessor) {
        return parent.getBaseColumn(x, y, accessor);
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {

    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long seed, BiomeManager biomeManager, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {

    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z) {
        return parent.getNoiseBiome(x, y, z);
    }

    @Override
    public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestMapFeature(ServerLevel level, HolderSet<ConfiguredStructureFeature<?, ?>> features, BlockPos pos, int $$3, boolean $$4) {
        return parent.findNearestMapFeature(level, features, pos, $$3, $$4);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunkAccess, StructureFeatureManager featureManager) {

    }

    @Override
    public boolean hasFeatureChunkInRange(ResourceKey<StructureSet> feature, long seed, int x, int y, int z) {
        return parent.hasFeatureChunkInRange(feature, seed, x, y, z);
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos pos) {
        return parent.getMobsAt(biome, structureFeatureManager, mobCategory, pos);
    }

    @Override
    public void createStructures(RegistryAccess registryAccess, StructureFeatureManager featureManager, ChunkAccess chunkAccess, StructureManager structureManager, long seed) {
        parent.createStructures(registryAccess, featureManager, chunkAccess, structureManager, seed);
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureFeatureManager featureManager, ChunkAccess chunkAccess) {
        parent.createReferences(level, featureManager, chunkAccess);
    }

    @Override
    public int getFirstFreeHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor heightAccessor) {
        return parent.getBaseHeight(x, z, types, heightAccessor);
    }

    @Override
    public int getFirstOccupiedHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor heightAccessor) {
        return parent.getBaseHeight(x, z, types, heightAccessor) - 1;
    }

    @Override
    public void ensureStructuresGenerated() {
        parent.ensureStructuresGenerated();
    }

    @Override
    public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement placement) {
        return parent.getRingPositionsFor(placement);
    }

    @Override
    public void addDebugScreenInfo(List<String> output, BlockPos pos) {
        parent.addDebugScreenInfo(output, pos);
    }

}
