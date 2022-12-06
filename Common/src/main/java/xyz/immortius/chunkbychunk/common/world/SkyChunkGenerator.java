package xyz.immortius.chunkbychunk.common.world;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import xyz.immortius.chunkbychunk.mixins.ChunkGeneratorStructureAccessor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * The Sky Chunk Generator - Sky Chunk Generators wrap a parent generator but disable actual generation. The
 * parent generator is retained for biome information and similar. Each sky chunk generator also has a reference
 * to the dimension that generates chunks for this dimension
 */
public class SkyChunkGenerator extends NoiseBasedChunkGenerator {

    public static final Codec<SkyChunkGenerator> CODEC = RecordCodecBuilder.create((encoded) ->
            encoded.group(ChunkGenerator.CODEC.withLifecycle(Lifecycle.stable()).fieldOf("parent").forGetter(SkyChunkGenerator::getParent))
                    .apply(encoded, encoded.stable(SkyChunkGenerator::new))
    );
    public static final Codec<SkyChunkGenerator> OLD_NETHER_CODEC = RecordCodecBuilder.create((encoded) ->
            encoded.group(ChunkGenerator.CODEC.withLifecycle(Lifecycle.stable()).fieldOf("parent").forGetter(SkyChunkGenerator::getParent))
                    .apply(encoded, encoded.stable(SkyChunkGenerator::new))
    );

    private final ChunkGenerator parent;
    private ResourceKey<Level> generationLevel;
    private List<ResourceKey<Level>> synchedLevels = new ArrayList<>();
    private int initialChunks;
    private boolean chunkSpawnerAllowed;
    private boolean randomChunkSpawnerAllowed;

    public boolean isChunkSpawnerAllowed() {
        return chunkSpawnerAllowed;
    }

    public boolean isRandomChunkSpawnerAllowed() {
        return randomChunkSpawnerAllowed;
    }

    public void addSynchLevel(ResourceKey<Level> dimension) {
        synchedLevels.add(dimension);
    }

    public List<ResourceKey<Level>> getSynchedLevels() {
        return synchedLevels;
    }

    public enum EmptyGenerationType {
        Normal,
        Sealed,
        Nether;

        private static final Map<String, EmptyGenerationType> STRING_LOOKUP;

        static {
            ImmutableMap.Builder<String, EmptyGenerationType> builder = new ImmutableMap.Builder<>();
            for (EmptyGenerationType value : EmptyGenerationType.values()) {
                builder.put(value.name().toLowerCase(Locale.ROOT), value);
            }
            STRING_LOOKUP = builder.build();
        }

        public static EmptyGenerationType getFromString(String asString) {
            return STRING_LOOKUP.getOrDefault(asString.toLowerCase(Locale.ROOT), Normal);
        }
    }

    private EmptyGenerationType generationType = EmptyGenerationType.Normal;

    private final Map<String, ResourceKey<Level>> biomeDimensions = new HashMap<>();

    /**
     * @param parent The chunkGenerator this generator is based on
     */
    public SkyChunkGenerator(ChunkGenerator parent) {
        super(((ChunkGeneratorStructureAccessor) parent).getStructureSet(), ChunkGeneratorAccess.getNoiseParamsRegistry(parent), parent.getBiomeSource(), ChunkGeneratorAccess.getNoiseGeneratorSettings(parent));
        this.parent = parent;
    }

    public void configure(ResourceKey<Level> generationLevel, EmptyGenerationType generationType, int initialChunks, boolean chunkSpawnerAllowed, boolean randomChunkSpawnerAllowed) {
        this.generationLevel = generationLevel;
        this.generationType = generationType;
        this.initialChunks = initialChunks;
        this.chunkSpawnerAllowed = chunkSpawnerAllowed;
        this.randomChunkSpawnerAllowed = randomChunkSpawnerAllowed;
    }

    public void addBiomeDimension(String name, ResourceKey<Level> level) {
        biomeDimensions.put(name, level);
    }

    public ResourceKey<Level> getBiomeDimension(String name) {
        return biomeDimensions.get(name);
    }

    public int getInitialChunks() {
        return initialChunks;
    }

    public ChunkGenerator getParent() {
        return parent;
    }

    public ResourceKey<Level> getGenerationLevel() {
        return generationLevel;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        return switch (generationType) {
            case Sealed -> parent.fillFromNoise(executor, blender, randomState, structureManager, chunk).whenCompleteAsync((chunkAccess, throwable) -> {
                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(0, 0, 0);
                for (blockPos.setZ(0); blockPos.getZ() < 16; blockPos.setZ(blockPos.getZ() + 1)) {
                    for (blockPos.setX(0); blockPos.getX() < 16; blockPos.setX(blockPos.getX() + 1)) {
                        blockPos.setY(chunkAccess.getMaxBuildHeight() - 1);
                        while (blockPos.getY() > chunkAccess.getMinBuildHeight() && chunkAccess.getBlockState(blockPos).getBlock() instanceof AirBlock) {
                            blockPos.setY(blockPos.getY() - 1);
                        }
                        while (blockPos.getY() > chunkAccess.getMinBuildHeight()) {
                            chunkAccess.setBlockState(blockPos, Blocks.BEDROCK.defaultBlockState(), false);
                            blockPos.setY(blockPos.getY() - 1);
                        }
                        chunkAccess.setBlockState(blockPos, Blocks.VOID_AIR.defaultBlockState(), false);
                    }
                }
            });
            case Nether -> CompletableFuture.completedFuture(chunk).whenCompleteAsync((chunkAccess, throwable) -> {
                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(0, 0, 0);
                for (blockPos.setZ(0); blockPos.getZ() < 16; blockPos.setZ(blockPos.getZ() + 1)) {
                    for (blockPos.setX(0); blockPos.getX() < 16; blockPos.setX(blockPos.getX() + 1)) {
                        blockPos.setY(chunkAccess.getMinBuildHeight());
                        chunkAccess.setBlockState(blockPos, Blocks.LAVA.defaultBlockState(), false);
                        blockPos.setY(chunkAccess.getMinBuildHeight() + 1);
                        chunkAccess.setBlockState(blockPos, Blocks.LAVA.defaultBlockState(), false);
                        blockPos.setY(127);
                        chunkAccess.setBlockState(blockPos, Blocks.BEDROCK.defaultBlockState(), false);
                    }
                }
            });
            default -> CompletableFuture.completedFuture(chunk);
        };
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
        parent.createStructures(registry, randomState, structureManager, chunk, structureTemplateManager, seed);
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
        parent.createReferences(level, structureManager, chunk);
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

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public BiomeGenerationSettings getBiomeGenerationSettings(Holder<Biome> biome) {
        return parent.getBiomeGenerationSettings(biome);
    }

    @Override
    protected List<StructurePlacement> getPlacementsForStructure(Holder<Structure> structure, RandomState state) {
        return ChunkGeneratorAccess.getPlacementsForStructure(parent, structure, state);
    }
}
