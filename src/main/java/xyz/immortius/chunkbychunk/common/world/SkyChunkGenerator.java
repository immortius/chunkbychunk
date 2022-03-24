package xyz.immortius.chunkbychunk.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * The prime Sky Chunk Generator - this generates the overworld dimension, which is either empty or is
 * a simple heightmap of bedrock based on generateSealedWorld.
 */
public class SkyChunkGenerator extends BaseSkyChunkGenerator {

    public static final Codec<SkyChunkGenerator> CODEC = RecordCodecBuilder.create((encoded) ->
            encoded.group(ChunkGenerator.CODEC.withLifecycle(Lifecycle.stable()).fieldOf("parent").forGetter((decoded) -> decoded.getParent()),
                            Codec.BOOL.withLifecycle(Lifecycle.stable()).fieldOf("sealed").forGetter((decoded) -> decoded.generateSealedWorld))
                    .apply(encoded, encoded.stable(SkyChunkGenerator::new))
    );

    private final boolean generateSealedWorld;

    /**
     * @param parent The chunkGenerator this generator is based on
     * @param generateSealedWorld Whether to generate a basic bedrock heightmap or not
     */
    public SkyChunkGenerator(ChunkGenerator parent, boolean generateSealedWorld) {
        super(parent, ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL);
        this.generateSealedWorld = generateSealedWorld;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return SkyChunkGenerator.CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return new SkyChunkGenerator(parent.withSeed(seed), generateSealedWorld);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunk) {
        if (generateSealedWorld) {
            return parent.fillFromNoise(executor, blender, structureFeatureManager, chunk).whenCompleteAsync((chunkAccess, throwable) -> {
                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(0,0,0);
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
        } else {
            return super.fillFromNoise(executor, blender, structureFeatureManager, chunk);
        }
    }
}
