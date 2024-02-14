package xyz.immortius.chunkbychunk.server.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import xyz.immortius.chunkbychunk.common.util.ChunkUtil;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.List;
import java.util.Random;

/**
 * Helper class for spawning a chunk. Spawning is done by copying a chunk from SkyChunkGeneration level
 * to the overworld. All blocks, block entities and other entities are copied. For best results the chunk being copied
 * should be a forced chunk on the SkyChunkGeneration end to ensure entities are loaded - at least for a little before
 * the copy until after the copy (takes at least a tick it seems)
 */
public final class SpawnChunkHelper {

    private SpawnChunkHelper() {
    }

    /**
     * Checks whether a chunk is 'empty'. A chunk is empty of it doesn't have bedrock on its lowest level.
     *
     * @param level    The level to check
     * @param chunkPos The chunk position to check
     * @return Whether the chunk is 'empty' and thus ready to be spawned into.
     */
    public static boolean isEmptyChunk(LevelAccessor level, ChunkPos chunkPos) {
        BlockPos bedrockCheckBlock = chunkPos.getMiddleBlockPosition(level.getMinBuildHeight());
        return !Blocks.BEDROCK.equals(level.getBlockState(bedrockCheckBlock).getBlock());
    }

    /**
     * Generates a Bedrock chest containing a chunk spawner at the bottom of a chunk
     *
     * @param targetLevel The level of the chunk
     * @param chunkPos    The position of the chunk
     */
    public static void createNextSpawner(ServerLevel targetLevel, ChunkPos chunkPos) {
        Random random = ChunkUtil.getChunkRandom(targetLevel, chunkPos);

        int minPos = Math.min(ChunkByChunkConfig.get().getGeneration().getMinChestSpawnDepth(), ChunkByChunkConfig.get().getGeneration().getMaxChestSpawnDepth());
        int maxPos = Math.max(ChunkByChunkConfig.get().getGeneration().getMinChestSpawnDepth(), ChunkByChunkConfig.get().getGeneration().getMaxChestSpawnDepth());;
        while (maxPos > minPos && (targetLevel.getBlockState(new BlockPos(chunkPos.getMiddleBlockX(), maxPos, chunkPos.getMiddleBlockZ())).getBlock() instanceof AirBlock)) {
            maxPos--;
        }
        int yPos;
        if (minPos == maxPos) {
            yPos = minPos;
        } else {
            yPos = random.nextInt(minPos, maxPos + 1);
        }
        int xPos = chunkPos.getMinBlockX() + random.nextInt(0, 16);
        int zPos = chunkPos.getMinBlockZ() + random.nextInt(0, 16);

        BlockPos blockPos = new BlockPos(xPos, yPos, zPos);
        if (ChunkByChunkConfig.get().getGeneration().useBedrockChest()) {
            targetLevel.setBlock(blockPos, Services.PLATFORM.bedrockChestBlock().defaultBlockState(), Block.UPDATE_CLIENTS);
        } else {
            targetLevel.setBlock(blockPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        if (targetLevel.getBlockEntity(blockPos) instanceof RandomizableContainerBlockEntity chestEntity) {
            List<ItemStack> items = ChunkByChunkConfig.get().getGeneration().getChestContents().getItems(random, ChunkByChunkConfig.get().getGeneration().getChestQuantity());
            for (int i = 0; i < items.size(); i++) {
                chestEntity.setItem(i, items.get(i));
            }
        }
    }

}
