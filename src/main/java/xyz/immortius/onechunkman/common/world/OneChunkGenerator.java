package xyz.immortius.onechunkman.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import xyz.immortius.onechunkman.OneChunkMan;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Custom chunk generator wrapping the standard overworld chunk generator but limiting the chunks spawned to a limited set - defaulting to one.
 * The standard chunk generator is wrapped so that it can be referred to for the purposes of later re-generating a chunk.
 *
 * The initial allowed chunk is chosen as the spawn point chunk for convenience of not immediately falling to your death - also hopefully
 * improves the quality of that single chunk
 */
public class OneChunkGenerator extends ChunkGenerator {

    public static final Codec<OneChunkGenerator> CODEC = RecordCodecBuilder.create((encoded) -> {
        return encoded.group(NoiseBasedChunkGenerator.CODEC.fieldOf("parent").forGetter((decoded) -> {
            return decoded.parent;
        })).apply(encoded, encoded.stable(OneChunkGenerator::new));
    });

    private final NoiseBasedChunkGenerator parent;
    private final Set<ChunkPos> allowedChunks = new HashSet<>();

    public OneChunkGenerator(NoiseBasedChunkGenerator parent) {
        super(parent.getBiomeSource(), parent.getSettings());
        this.parent = parent;
        allowedChunks.add(new ChunkPos(climateSampler().findSpawnPosition()));
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return OneChunkGenerator.CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return new OneChunkGenerator((NoiseBasedChunkGenerator) parent.withSeed(seed));
    }

    @Override
    public Climate.Sampler climateSampler() {
        return parent.climateSampler();
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, BiomeManager biomeManager, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
        if (allowedChunks.contains(region.getCenter())) {
            parent.applyCarvers(region, seed, biomeManager, structureFeatureManager, chunkAccess, carving);
        }
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        if (allowedChunks.contains(region.getCenter())) {
            parent.buildSurface(region, structureFeatureManager, chunkAccess);
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        if (allowedChunks.contains(region.getCenter())) {
            parent.spawnOriginalMobs(region);
        }
    }

    @Override
    public int getGenDepth() {
        return parent.getGenDepth();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        if (allowedChunks.contains(chunkAccess.getPos())) {
            CompletableFuture<ChunkAccess> parentFuture = parent.fillFromNoise(executor, blender, structureFeatureManager, chunkAccess);
            return parentFuture.whenCompleteAsync((access,y) -> access.setBlockState(new BlockPos(8,-60,8), OneChunkMan.SPAWN_CHUNK_BLOCK.get().defaultBlockState(), true), executor);
        } else {
            CompletableFuture<ChunkAccess> parentFuture = parent.fillFromNoise(executor, blender, structureFeatureManager, chunkAccess);
            return parentFuture.whenCompleteAsync((access,throwable) -> {
                for (int z = access.getPos().getMinBlockZ(); z <= access.getPos().getMaxBlockZ(); z++) {
                    for (int x = access.getPos().getMinBlockX(); x <= access.getPos().getMaxBlockX(); x++) {
                        int y = access.getMaxBuildHeight();
                        while (access.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof AirBlock) {
                            y--;
                        }
                        while (y > access.getMinBuildHeight()) {
                            access.setBlockState(new BlockPos(x, y, z), Blocks.BEDROCK.defaultBlockState(), false);
                            y--;
                        }
                    }
                }
                access.setBlockState(new BlockPos(8,-60,8), OneChunkMan.SPAWN_CHUNK_BLOCK.get().defaultBlockState(), true);
            }, executor);
        }
    }

    @Override
    public void createStructures(RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long seed) {
       // if (allowedChunks.contains(chunkAccess.getPos())) {
            parent.createStructures(registryAccess, structureFeatureManager, chunkAccess, structureManager, seed);
        //}
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        //if (allowedChunks.contains(chunkAccess.getPos())) {
            parent.createReferences(level, structureFeatureManager, chunkAccess);
        //}
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunkAccess, StructureFeatureManager structureFeatureManager) {
        if (allowedChunks.contains(chunkAccess.getPos())) {
            parent.applyBiomeDecoration(level, chunkAccess, structureFeatureManager);
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
    public int getBaseHeight(int x, int z, Heightmap.Types heightMapType, LevelHeightAccessor levelHeightAccessor) {
        return parent.getBaseHeight(x, z, heightMapType, levelHeightAccessor);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor) {
        return parent.getBaseColumn(x, z, levelHeightAccessor);
    }

    public void allowChunk(ChunkPos chunkPos) {
        allowedChunks.add(chunkPos);
    }


}
