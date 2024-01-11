package xyz.immortius.chunkbychunk.server.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.config.ChunkRewardChestContent;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.Random;

/**
 * Helper class for spawning a chunk. Spawning is done by copying a chunk from
 * SkyChunkGeneration level
 * to the overworld. All blocks, block entities and other entities are copied.
 * For best results the chunk being copied
 * should be a forced chunk on the SkyChunkGeneration end to ensure entities are
 * loaded - at least for a little before
 * the copy until after the copy (takes at least a tick it seems)
 */
public final class SpawnChunkHelper {

    // TODO: Switch to better random source
    private static final Random random = new Random();

    private SpawnChunkHelper() {
    }

    /**
     * Checks whether a chunk is 'empty'. A chunk is empty of it doesn't have
     * bedrock on its lowest level.
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
        int minDepth = ChunkByChunkConfig.get().getGeneration().getMinChestSpawnDepth();
        int maxDepth = ChunkByChunkConfig.get().getGeneration().getMaxChestSpawnDepth();

        int minPos = Math.min(minDepth, maxDepth);
        int maxPos = Math.max(minDepth, maxDepth);

        // 13x13 rectangle 1/169=0.0059
        int xPos = random.nextInt(chunkPos.getMaxBlockX() - 13, chunkPos.getMaxBlockX() - 2);
        int yPos = maxPos;
        int zPos = random.nextInt(chunkPos.getMaxBlockZ() - 13, chunkPos.getMaxBlockZ() - 2);

        int chestRegionY = Math.abs(maxPos - minPos) + 1;
        int maxGenHeight = targetLevel.getMaxBuildHeight() - 1;// 319 for v1.20.1
        int minGenHeight = targetLevel.getMinBuildHeight() + 1; // -63

        if (chestRegionY < 8) {
            chestRegionY = 8;
        }

        BlockPos.MutableBlockPos detectedBlockPos = new BlockPos.MutableBlockPos();
        do {
            maxGenHeight--;
            detectedBlockPos.set(xPos, maxGenHeight, zPos);
            if (maxGenHeight < minGenHeight) {
                maxGenHeight = minGenHeight + chestRegionY;
                break;
            }
        } while (targetLevel.getBlockState(detectedBlockPos).getBlock() instanceof AirBlock);

        if (Math.abs(maxGenHeight - minGenHeight) > chestRegionY / 2) {
            maxGenHeight -= chestRegionY;
        }

        int chestRandomYPos[] = new int[] {
                random.nextInt(minGenHeight, minGenHeight + chestRegionY),
                random.nextInt(maxGenHeight - chestRegionY, maxGenHeight)
        };

        yPos = chestRandomYPos[random.nextInt(0, 2)];

        BlockPos blockPos = new BlockPos(xPos, yPos, zPos);
        if (ChunkByChunkConfig.get().getGeneration().useBedrockChest()) {
            targetLevel.setBlock(blockPos, Services.PLATFORM.bedrockChestBlock().defaultBlockState(),
                    Block.UPDATE_CLIENTS);
        } else {
            targetLevel.setBlock(blockPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        if (targetLevel.getBlockEntity(blockPos) instanceof RandomizableContainerBlockEntity chestEntity) {

            ChunkRewardChestContent item = ChunkByChunkConfig.get().getGeneration().getChestContents();

            if (item == ChunkRewardChestContent.UnstableChunkSpawner) {
                // default item
                chestEntity.setItem(0, ChunkRewardChestContent.UnstableChunkSpawner
                        .getItem(random.nextInt(1, 5)));

                int itemSeed = random.nextInt(1, 101);
                // 1 to 95
                if (itemSeed > 0 && itemSeed <= 95) {
                    chestEntity.setItem(1, ChunkRewardChestContent.WorldFragment
                            .getItem(random.nextInt(1, 17)));
                }
                // 96 to 100
                if (itemSeed > 95 && itemSeed <= 100) {
                    chestEntity.setItem(1, ChunkRewardChestContent.WorldForge
                            .getItem(1));
                }
            } else {
                chestEntity.setItem(0, item.getItem(ChunkByChunkConfig.get().getGeneration().getChestQuantity()));
            }
        }

    }
}
