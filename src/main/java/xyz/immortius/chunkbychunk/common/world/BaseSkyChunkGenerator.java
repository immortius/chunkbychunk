package xyz.immortius.chunkbychunk.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * The Base Sky Chunk Generator - Sky Chunk Generators wrap a parent generator but disable actual generation. The
 * parent generator is retained for biome information and similar. Each sky chunk generator also has a reference
 * to the dimension that generates chunks for this dimension
 */
public abstract class BaseSkyChunkGenerator extends ChunkGenerator {

    protected final ChunkGenerator parent;
    protected final ResourceKey<Level> generationLevel;

    /**
     * @param parent The chunkGenerator this generator is based on
     */
    public BaseSkyChunkGenerator(ChunkGenerator parent, ResourceKey<Level> generationLevel) {
        super(parent.getBiomeSource(), parent.getSettings());
        this.parent = parent;
        this.generationLevel = generationLevel;
    }

    /**
     * @param parent The chunkGenerator this generator is based on
     * @param structureSettings Structure settings to use, if not from the parent generator
     */
    public BaseSkyChunkGenerator(ChunkGenerator parent, ResourceKey<Level> generationLevel, StructureSettings structureSettings) {
        super(parent.getBiomeSource(), structureSettings);
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
    public Climate.Sampler climateSampler() {
        return parent.climateSampler();
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, BiomeManager biomeManager, StructureFeatureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {

    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureFeatureManager structureFeatureManager, ChunkAccess chunk) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {

    }

    @Override
    public int getGenDepth() {
        return parent.getGenDepth();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunk) {
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
    public int getBaseHeight(int x, int z, Heightmap.Types heightMapType, LevelHeightAccessor heightAccessor) {
        return parent.getBaseHeight(x, z, heightMapType, heightAccessor);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor) {
        return parent.getBaseColumn(x, z, heightAccessor);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> biomes, Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunk) {
        return parent.createBiomes(biomes, executor, blender, structureFeatureManager, chunk);
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        return parent.getNoiseBiome(x, y, z);
    }

    @Override
    public BlockPos findNearestMapFeature(ServerLevel level, StructureFeature<?> feature, BlockPos pos, int p_62165_, boolean p_62166_) {
        return parent.findNearestMapFeature(level, feature, pos, p_62165_, p_62166_);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureFeatureManager structureFeatureManager) {

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
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos pos) {
        return parent.getMobsAt(biome, structureFeatureManager, mobCategory, pos);
    }

    @Override
    public void createStructures(RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunk, StructureManager structureManager, long seed) {

    }

    @Override
    public void createReferences(WorldGenLevel level, StructureFeatureManager structureFeatureManager, ChunkAccess chunk) {

    }

    @Override
    public int getFirstFreeHeight(int x, int z, Heightmap.Types heightMapType, LevelHeightAccessor heightAccessor) {
        return parent.getFirstFreeHeight(x, z, heightMapType, heightAccessor);
    }

    @Override
    public int getFirstOccupiedHeight(int x, int z, Heightmap.Types heightMapType, LevelHeightAccessor heightAccessor) {
        return parent.getFirstOccupiedHeight(x, z, heightMapType, heightAccessor);
    }

    @Override
    public boolean hasStronghold(ChunkPos chunkPos) {
        return parent.hasStronghold(chunkPos);
    }
}
