package xyz.immortius.chunkbychunk.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * The prime Sky Chunk Generator - this generates the overworld dimension, which is either empty or is
 * a simple heightmap of bedrock based on generateSealedWorld.
 */
public class SkyChunkGenerator extends ChunkGenerator {

    public static final Codec<SkyChunkGenerator> CODEC = RecordCodecBuilder.create((encoded) ->
            encoded.group(ChunkGenerator.CODEC.withLifecycle(Lifecycle.stable()).fieldOf("parent").forGetter((decoded) -> decoded.parent),
                            Codec.BOOL.withLifecycle(Lifecycle.stable()).fieldOf("sealed").forGetter((decoded) -> decoded.generateSealedWorld))
                    .apply(encoded, encoded.stable(SkyChunkGenerator::new))
    );

    private final ChunkGenerator parent;
    private final boolean generateSealedWorld;

    /**
     * @param parent The chunkGenerator this generator is based on
     * @param generateSealedWorld Whether to generate a basic bedrock heightmap or not
     */
    public SkyChunkGenerator(ChunkGenerator parent, boolean generateSealedWorld) {
        super(parent.getBiomeSource(), parent.getSettings());
        this.parent = parent;
        this.generateSealedWorld = generateSealedWorld;
    }

    public ChunkGenerator getParent() {
        return parent;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return SkyChunkGenerator.CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long p_62156_) {
        return new SkyChunkGenerator(parent.withSeed(p_62156_), generateSealedWorld);
    }

    @Override
    public Climate.Sampler climateSampler() {
        return parent.climateSampler();
    }

    @Override
    public void applyCarvers(WorldGenRegion p_187691_, long p_187692_, BiomeManager p_187693_, StructureFeatureManager p_187694_, ChunkAccess p_187695_, GenerationStep.Carving p_187696_) {

    }

    @Override
    public void buildSurface(WorldGenRegion p_187697_, StructureFeatureManager p_187698_, ChunkAccess p_187699_) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion p_62167_) {

    }

    @Override
    public int getGenDepth() {
        return parent.getGenDepth();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_187748_, Blender p_187749_, StructureFeatureManager p_187750_, ChunkAccess p_187751_) {
        if (generateSealedWorld) {
            return parent.fillFromNoise(p_187748_, p_187749_, p_187750_, p_187751_).whenCompleteAsync((chunkAccess, throwable) -> {

                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int y = chunkAccess.getMaxBuildHeight();
                        while (y > chunkAccess.getMinBuildHeight() && chunkAccess.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof AirBlock) {
                            y--;
                        }
                        while (y > chunkAccess.getMinBuildHeight()) {
                            chunkAccess.setBlockState(new BlockPos(x, y, z), Blocks.BEDROCK.defaultBlockState(), false);
                            y--;
                        }
                    }
                }
            });
        } else {
            return CompletableFuture.completedFuture(p_187751_);
        }
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
    public int getBaseHeight(int p_156153_, int p_156154_, Heightmap.Types p_156155_, LevelHeightAccessor p_156156_) {
        return parent.getBaseHeight(p_156153_, p_156154_, p_156155_, p_156156_);
    }

    @Override
    public NoiseColumn getBaseColumn(int p_156150_, int p_156151_, LevelHeightAccessor p_156152_) {
        return parent.getBaseColumn(p_156150_, p_156151_, p_156152_);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> p_196743_, Executor p_196744_, Blender p_196745_, StructureFeatureManager p_196746_, ChunkAccess p_196747_) {
        return parent.createBiomes(p_196743_, p_196744_, p_196745_, p_196746_, p_196747_);
    }

    @Override
    public Biome getNoiseBiome(int p_187755_, int p_187756_, int p_187757_) {
        return parent.getNoiseBiome(p_187755_, p_187756_, p_187757_);
    }

    @Override
    public BlockPos findNearestMapFeature(ServerLevel p_62162_, StructureFeature<?> p_62163_, BlockPos p_62164_, int p_62165_, boolean p_62166_) {
        return parent.findNearestMapFeature(p_62162_, p_62163_, p_62164_, p_62165_, p_62166_);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel p_187712_, ChunkAccess p_187713_, StructureFeatureManager p_187714_) {

    }

    @Override
    public StructureSettings getSettings() {
        return parent.getSettings();
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor p_156157_) {
        return parent.getSpawnHeight(p_156157_);
    }

    @Override
    public BiomeSource getBiomeSource() {
        return parent.getBiomeSource();
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome p_156158_, StructureFeatureManager p_156159_, MobCategory p_156160_, BlockPos p_156161_) {
        return parent.getMobsAt(p_156158_, p_156159_, p_156160_, p_156161_);
    }

    @Override
    public void createStructures(RegistryAccess p_62200_, StructureFeatureManager p_62201_, ChunkAccess p_62202_, StructureManager p_62203_, long p_62204_) {

    }

    @Override
    public void createReferences(WorldGenLevel p_62178_, StructureFeatureManager p_62179_, ChunkAccess p_62180_) {

    }

    @Override
    public int getFirstFreeHeight(int p_156175_, int p_156176_, Heightmap.Types p_156177_, LevelHeightAccessor p_156178_) {
        return parent.getFirstFreeHeight(p_156175_, p_156176_, p_156177_, p_156178_);
    }

    @Override
    public int getFirstOccupiedHeight(int p_156180_, int p_156181_, Heightmap.Types p_156182_, LevelHeightAccessor p_156183_) {
        return parent.getFirstOccupiedHeight(p_156180_, p_156181_, p_156182_, p_156183_);
    }

    @Override
    public boolean hasStronghold(ChunkPos p_62173_) {
        return parent.hasStronghold(p_62173_);
    }
}
