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
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import xyz.immortius.chunkbychunk.interop.CBCInteropMethods;

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
        super(CBCInteropMethods.getStructureSets(parent), CBCInteropMethods.getNoiseParamsRegistry(parent), parent.getBiomeSource(), CBCInteropMethods.getNoiseGeneratorSettings(parent));
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
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> biomes, Executor executor, RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunk) {
        return parent.createBiomes(biomes, executor, randomState, blender, structureManager, chunk);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long p_223044_, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving carving) {
    }

    @Override
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel level, HolderSet<Structure> structure, BlockPos pos, int p_223041_, boolean p_223042_) {
        return parent.findNearestMapStructure(level, structure, pos, p_223041_, p_223042_);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel p_223087_, ChunkAccess p_223088_, StructureManager p_223089_) {
    }

    @Override
    public boolean hasStructureChunkInRange(Holder<StructureSet> structureSet, RandomState randomState, long p_223144_, int p_223145_, int p_223146_, int p_223147_) {
        return parent.hasStructureChunkInRange(structureSet, randomState, p_223144_, p_223145_, p_223146_, p_223147_);
    }


    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
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
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> biome, StructureManager structureManager, MobCategory mobCategory, BlockPos pos) {
        return parent.getMobsAt(biome, structureManager, mobCategory, pos);
    }

    @Override
    public void createStructures(RegistryAccess registry, RandomState randomState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager structureTemplateManager, long seed) {
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
        parent.createReferences(level, structureManager, chunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
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
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return parent.getBaseHeight(x, z, type, heightAccessor, randomState);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return parent.getBaseColumn(x, z, heightAccessor, randomState);
    }

    @Override
    public int getFirstFreeHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return parent.getBaseHeight(x, z, type, heightAccessor, randomState);
    }

    @Override
    public int getFirstOccupiedHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return parent.getBaseHeight(x, z, type, heightAccessor, randomState) - 1;
    }

    @Override
    public void ensureStructuresGenerated(RandomState randomState) {
        parent.ensureStructuresGenerated(randomState);
    }

    @Override
    public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement placement, RandomState randomState) {
        return parent.getRingPositionsFor(placement, randomState);
    }

    @Override
    public void addDebugScreenInfo(List<String> outDebugInfo, RandomState randomState, BlockPos pos) {
        parent.addDebugScreenInfo(outDebugInfo, randomState, pos);
    }

    /** @deprecated */
    @Deprecated
    @Override
    public BiomeGenerationSettings getBiomeGenerationSettings(Holder<Biome> biome) {
        return parent.getBiomeGenerationSettings(biome);
    }
}
