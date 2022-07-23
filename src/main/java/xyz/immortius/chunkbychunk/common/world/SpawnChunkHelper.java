package xyz.immortius.chunkbychunk.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.CBCInteropMethods;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Helper class for spawning a chunk. Spawning is done by copying a chunk from SkyChunkGeneration level
 * to the overworld. All blocks, block entities and other entities are copied. For best results the chunk being copied
 * should be a forced chunk on the SkyChunkGeneration end to ensure entities are loaded - at least for a little before
 * the copy until after the copy (takes at least a tick it seems)
 */
public final class SpawnChunkHelper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random random = new Random();

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
        BlockPos bedrockCheckBlock = chunkPos.getBlockAt(8, level.getMinBuildHeight(), 8);
        return !Blocks.BEDROCK.equals(level.getBlockState(bedrockCheckBlock).getBlock());
    }

    /**
     * @param level The level to check
     * @return Whether the level is appropriate for spawning chunks - is it a SkyChunkGenerator level.
     */
    public static boolean isValidForChunkSpawn(ServerLevel level) {
        return level != null && level.getChunkSource().getGenerator() instanceof BaseSkyChunkGenerator;
    }

    /**
     * Spawns a chunk. This is done by copying information from the associated generation level
     *
     * @param targetLevel The level to spawn the chunk in
     * @param chunkPos    The position to spawn the chunk (both source and target)
     */
    public static void spawnChunkBlocks(ServerLevel targetLevel, ChunkPos chunkPos) {
        spawnChunkBlocks(targetLevel, chunkPos, chunkPos);
    }

    /**
     * Spawns the blocks for a chunk. This is done by copying information from the associated generation level
     *
     * @param targetLevel    The level to spawn the chunk in
     * @param sourceChunkPos The position of the chunk in the source dimension to pull from
     * @param targetChunkPos The position of the chunk in the target dimension to spawn
     */
    public static void spawnChunkBlocks(ServerLevel targetLevel, ChunkPos sourceChunkPos, ChunkPos targetChunkPos) {
        if (targetLevel.getChunkSource().getGenerator() instanceof BaseSkyChunkGenerator generator) {
            ServerLevel sourceLevel = Objects.requireNonNull(targetLevel.getServer()).getLevel(generator.getGenerationLevel());
            if (sourceLevel != null) {
                spawnChunkBlocks(targetLevel, targetChunkPos, sourceLevel, sourceChunkPos);
            }
        } else {
            LOGGER.warn("Attempted to spawn a chunk in a non-SkyChunk world");
        }
    }

    /**
     * Spawns the blocks for a chunk.
     * @param targetLevel    The level to spawn the chunk in
     * @param targetChunkPos The position of the chunk in the target dimension to spawn
     * @param sourceLevel    The level to spawn the chunk from
     * @param sourceChunkPos The position of the chunk in the source dimension to pull from
     */
    public static void spawnChunkBlocks(ServerLevel targetLevel, ChunkPos targetChunkPos, ServerLevel sourceLevel, ChunkPos sourceChunkPos) {
        if (!isValidForChunkSpawn(targetLevel)) {
            LOGGER.warn("Attempted to spawn a chunk in a non-SkyChunk world");
            return;
        }
        copyBlocks(sourceLevel, sourceChunkPos, targetLevel, targetChunkPos);
        if (ChunkByChunkConfig.get().getGeneration().spawnNewChunkChest()) {
            createNextSpawner(targetLevel, targetChunkPos);
        }
    }

    /**
     * Spawns a chunk. This is done by copying information from the SKY_CHUNK_GENERATION level
     *
     * @param targetLevel    The level to spawn the chunk in
     * @param sourceChunkPos The position of the chunk in the source dimension to pull from
     * @param targetChunkPos The position of the chunk in the target dimension to spawn
     */
    public static void spawnChunkEntities(ServerLevel targetLevel, ChunkPos sourceChunkPos, ChunkPos targetChunkPos) {
        if (targetLevel.getChunkSource().getGenerator() instanceof BaseSkyChunkGenerator generator) {
            ServerLevel sourceLevel = Objects.requireNonNull(targetLevel.getServer()).getLevel(generator.getGenerationLevel());
            if (sourceLevel != null) {
                copyEntities(sourceLevel, sourceChunkPos, targetLevel, targetChunkPos);
            }
        } else {
            LOGGER.warn("Attempted to spawn a chunk in a non-SkyChunk world");
        }
    }

    /**
     * Copies all blocks from one level to another, as long as there isn't an existing block that
     * shouldn't be overwritten.
     * Block entities will also be copied.
     *
     * @param from           The level to copy from
     * @param sourceChunkPos the chunk position to copy from
     * @param to             The level to copy to
     * @param targetChunkPos The position of the chunk to copy to
     */
    private static void copyBlocks(ServerLevel from, ChunkPos sourceChunkPos, ServerLevel to, ChunkPos targetChunkPos) {
        BlockPos.MutableBlockPos sourceBlock = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos targetBlock = new BlockPos.MutableBlockPos();
        int xOffset = targetChunkPos.getMinBlockX() - sourceChunkPos.getMinBlockX();
        int zOffset = targetChunkPos.getMinBlockZ() - sourceChunkPos.getMinBlockZ();
        for (int z = targetChunkPos.getMinBlockZ(); z <= targetChunkPos.getMaxBlockZ(); z++) {
            for (int x = targetChunkPos.getMinBlockX(); x <= targetChunkPos.getMaxBlockX(); x++) {
                targetBlock.set(x, to.getMinBuildHeight(), z);
                to.setBlock(targetBlock, Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        for (int y = to.getMinBuildHeight() + 1; y < to.getMaxBuildHeight() - 1; y++) {
            for (int z = sourceChunkPos.getMinBlockZ(); z <= sourceChunkPos.getMaxBlockZ(); z++) {
                for (int x = sourceChunkPos.getMinBlockX(); x <= sourceChunkPos.getMaxBlockX(); x++) {
                    sourceBlock.set(x, y, z);
                    targetBlock.set(x + xOffset, y, z + zOffset);
                    Block existingBlock = to.getBlockState(targetBlock).getBlock();
                    if (existingBlock instanceof LeavesBlock || existingBlock instanceof AirBlock || existingBlock instanceof LiquidBlock || existingBlock == Blocks.BEDROCK || existingBlock == Blocks.COBBLESTONE) {
                        to.setBlock(targetBlock, from.getBlockState(sourceBlock), Block.UPDATE_ALL);
                        BlockEntity fromBlockEntity = from.getBlockEntity(sourceBlock);
                        BlockEntity toBlockEntity = to.getBlockEntity(targetBlock);
                        if (fromBlockEntity != null && toBlockEntity != null) {
                            toBlockEntity.load(fromBlockEntity.saveWithFullMetadata());
                        }
                    }
                }
            }
        }
    }

    /**
     * Teleports all entities in a chunk from one level to another. Entities must have been loaded already.
     *
     * @param from           The level to teleport entities from
     * @param sourceChunkPos The position of the chunk to teleport entities from
     * @param to             The level to teleport entities to
     * @param targetChunkPos The chunk to teleport entities to
     */
    public static void copyEntities(ServerLevel from, ChunkPos sourceChunkPos, ServerLevel to, ChunkPos targetChunkPos) {
        List<Entity> entities = from.getEntities((Entity) null, new AABB(sourceChunkPos.getMinBlockX(), from.getMinBuildHeight(), sourceChunkPos.getMinBlockZ(), sourceChunkPos.getMaxBlockX(), from.getMaxBuildHeight(), sourceChunkPos.getMaxBlockZ()), (x) -> true);
        for (Entity e : entities) {
            Vec3 pos = new Vec3(e.getX() + (targetChunkPos.x - sourceChunkPos.x) * 16, e.getY(), e.getZ() + (targetChunkPos.z - sourceChunkPos.z) * 16);

            Entity movedEntity = CBCInteropMethods.changeDimension(e, to, new PortalInfo(pos, Vec3.ZERO, e.xRotO, e.yRotO));
            if (movedEntity != null) {
                movedEntity.setPos(pos);
            }
        }
    }

    /**
     * Generates a Bedrock chest containing a chunk spawner at the bottom of a chunk
     *
     * @param targetLevel The level of the chunk
     * @param chunkPos    The position of the chunk
     */
    private static void createNextSpawner(ServerLevel targetLevel, ChunkPos chunkPos) {
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

        BlockPos blockPos = new BlockPos(chunkPos.getMiddleBlockX(), yPos, chunkPos.getMiddleBlockZ());
        if (ChunkByChunkConfig.get().getGeneration().useBedrockChest()) {
            targetLevel.setBlock(blockPos, ChunkByChunkConstants.bedrockChestBlock().defaultBlockState(), Block.UPDATE_ALL);
        } else {
            targetLevel.setBlock(blockPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (targetLevel.getBlockEntity(blockPos) instanceof RandomizableContainerBlockEntity chestEntity) {
            chestEntity.setItem(0, ChunkByChunkConfig.get().getGeneration().getChestContents().getItem(ChunkByChunkConfig.get().getGeneration().getChestQuantity()));
        }
    }

}
