package xyz.immortius.chunkbychunk.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;

import java.util.Random;
import java.util.Set;

/**
 * Utility methods for working with chunks
 */
public final class ChunkUtil {
    private ChunkUtil() {

    }

    public static Random getChunkRandom(ServerLevel targetLevel, ChunkPos chunkPos) {
        long seed = targetLevel.getSeed() + ChunkByChunkConstants.MOD_ID.hashCode();
        Random random = new Random(seed);
        long xModifier = random.nextLong();
        long yModifier = random.nextLong();
        long chunkSeed = (long) chunkPos.x & xModifier ^ (long) chunkPos.z * yModifier ^ seed;
        random.setSeed(chunkSeed);
        return random;
    }

    /**
     * Finds a safe spawn height by finding the first solid block down from the sky. (This won't work well
     * @param chunk The chunk to check
     * @param x The x coord
     * @param z The z coord
     * @return A y coord that is available for spawns
     */
    public static int getSafeSpawnHeight(ChunkAccess chunk, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x,chunk.getMaxBuildHeight() - 1,z);
        // Find some space first
        while (pos.getY() > chunk.getMinBuildHeight()) {
            BlockState blockState = chunk.getBlockState(pos);
            // I full expect isPossibleToRespawnInThis to become a static method in a future version
            if (blockState.getBlock().isPossibleToRespawnInThis(blockState)) {
                break;
            }
            pos.setY(pos.getY() - 1);
        }
        // Now find the ground
        while (pos.getY() > chunk.getMinBuildHeight()) {
            BlockState blockState = chunk.getBlockState(pos);
            if (!blockState.getBlock().isPossibleToRespawnInThis(blockState)) {
                return pos.getY() + 1;
            }
            pos.setY(pos.getY() - 1);
        }
        return pos.getY();
    }

    /**
     * @param chunk The chunk to check
     * @param blocks The set of blocks to count
     * @return The number of matching blocks
     */
    public static int countBlocks(ChunkAccess chunk, Set<Block> blocks) {
        if (blocks.size() == 0) {
            return 0;
        }
        if (blocks.size() == 1) {
            return countBlocks(chunk, blocks.stream().findFirst().get());
        }
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0,0,0);
        int count = 0;
        for (pos.setX(chunkPos.getMinBlockX()); pos.getX() <= chunkPos.getMaxBlockX(); pos.setX(pos.getX() + 1)) {
            for (pos.setY(chunk.getMinBuildHeight()); pos.getY() <= chunk.getMaxBuildHeight() - 1; pos.setY(pos.getY() + 1)) {
                for (pos.setZ(chunkPos.getMinBlockZ()); pos.getZ() <= chunkPos.getMaxBlockZ(); pos.setZ(pos.getZ() + 1)) {
                    if (blocks.contains(chunk.getBlockState(pos).getBlock())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * @param chunk The chunk to check
     * @param block The block to count
     * @return The number of matching blocks
     */
    public static int countBlocks(ChunkAccess chunk, Block block) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0,0,0);
        int count = 0;
        for (pos.setX(chunkPos.getMinBlockX()); pos.getX() <= chunkPos.getMaxBlockX(); pos.setX(pos.getX() + 1)) {
            for (pos.setY(chunk.getMinBuildHeight()); pos.getY() <= chunk.getMaxBuildHeight() - 1; pos.setY(pos.getY() + 1)) {
                for (pos.setZ(chunkPos.getMinBlockZ()); pos.getZ() <= chunkPos.getMaxBlockZ(); pos.setZ(pos.getZ() + 1)) {
                    if (chunk.getBlockState(pos).getBlock() == block) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * @param chunk The chunk to check
     * @param blockTag The block to count
     * @return The number of matching blocks
     */
    public static int countBlocks(ChunkAccess chunk, TagKey<Block> blockTag) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0,0,0);

        int count = 0;
        for (pos.setX(chunkPos.getMinBlockX()); pos.getX() <= chunkPos.getMaxBlockX(); pos.setX(pos.getX() + 1)) {
            for (pos.setY(chunk.getMinBuildHeight()); pos.getY() <= chunk.getMaxBuildHeight(); pos.setY(pos.getY() + 1)) {
                for (pos.setZ(chunkPos.getMinBlockZ()); pos.getZ() <= chunkPos.getMaxBlockZ(); pos.setZ(pos.getZ() + 1)) {
                    if (chunk.getBlockState(pos).is(blockTag)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
