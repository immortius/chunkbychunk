package xyz.immortius.chunkbychunk.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * This sky chunk generator is intended for the nether. It generates "empty" chunks with a layer of bedrock at typical
 * ceiling height for the nether, and lava at the bottom of the chunk.
 */
public class NetherChunkByChunkGenerator extends BaseSkyChunkGenerator {

    public static final Codec<NetherChunkByChunkGenerator> CODEC = RecordCodecBuilder.create((encoded) ->
            encoded.group(ChunkGenerator.CODEC.withLifecycle(Lifecycle.stable()).fieldOf("parent").forGetter(BaseSkyChunkGenerator::getParent))
                    .apply(encoded, encoded.stable(NetherChunkByChunkGenerator::new))
    );

    /**
     * @param parent The chunkGenerator this generator is based on
     */
    public NetherChunkByChunkGenerator(ChunkGenerator parent) {
        super(parent, ChunkByChunkConstants.NETHER_CHUNK_GENERATION_LEVEL);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return NetherChunkByChunkGenerator.CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return new NetherChunkByChunkGenerator(parent.withSeed(seed));
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunk) {
        return super.fillFromNoise(executor, blender, structureFeatureManager, chunk).whenCompleteAsync((chunkAccess, throwable) -> {
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
    }

}
