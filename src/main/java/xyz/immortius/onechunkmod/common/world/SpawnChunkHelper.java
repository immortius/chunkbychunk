package xyz.immortius.onechunkmod.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.onechunkmod.OneChunkMod;
import xyz.immortius.onechunkmod.common.blockEntities.BedrockChestBlockEntity;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Helper class for spawning a chunk in OneChunkMod. Spawning is done by copying a chunk from SkyChunkGeneration level
 * to the overworld. All blocks, block entities and other entities are copied. For best results the chunk being copied
 * should be a forced chunk on the SkyChunkGeneration end to ensure entities are loaded - at least for a little before
 * the copy until after the copy.
 */
public final class SpawnChunkHelper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityTeleport ENTITY_TELEPORT = new EntityTeleport();

    private SpawnChunkHelper() {
    }

    /**
     * Checks whether a chunk is 'empty'. A chunk is empty of it doesn't have bedrock on its lowest level.
     * @param level The level to check
     * @param chunkPos The chunk position to check
     * @return Whether the chunk is 'empty' and thus ready to be spawned into.
     */
    public static boolean isEmptyChunk(ServerLevel level, ChunkPos chunkPos) {
        BlockPos bedrockCheckBlock = chunkPos.getBlockAt(8, level.getMinBuildHeight(), 8);
        return !Blocks.BEDROCK.equals(level.getBlockState(bedrockCheckBlock).getBlock());
    }

    /**
     * @param level The level to check
     * @return Whether the level is appropriate for spawning chunks - is it a SkyChunkGenerator level.
     */
    public static boolean isValidForChunkSpawn(ServerLevel level) {
        return level.getChunkSource().getGenerator() instanceof SkyChunkGenerator;
    }

    /**
     * Spawns a chunk. This is done by copying information from the SKY_CHUNK_GENERATION level
     * @param targetLevel The level to spawn the chunk in
     * @param chunkPos The position of the chunk to spawn
     */
    public static void spawnChunk(ServerLevel targetLevel, ChunkPos chunkPos) {
        if (!isValidForChunkSpawn(targetLevel)) {
            LOGGER.warn("Attempted to spawn a chunk in a non-SkyChunk world");
            return;
        }
        ServerLevel sourceLevel = Objects.requireNonNull(targetLevel.getServer()).getLevel(OneChunkMod.SKY_CHUNK_GENERATION_LEVEL);
        if (sourceLevel != null) {
            copyBlocks(sourceLevel, targetLevel, chunkPos);
            copyEntities(sourceLevel, targetLevel, chunkPos);
            createNextSpawner(targetLevel, chunkPos);
        }
    }

    /**
     * Copies all blocks from one level to another, as long as there isn't an existing block that
     * shouldn't be overwritten.
     * Block entities will also be copied.
     * @param from The level to copy from
     * @param to The level to copy to
     * @param chunkPos The position of the chunk to copy
     */
    private static void copyBlocks(ServerLevel from, ServerLevel to, ChunkPos chunkPos) {
        for (int y = to.getMinBuildHeight(); y < to.getMaxBuildHeight(); y++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block existingBlock = to.getBlockState(pos).getBlock();
                    if (existingBlock instanceof LeavesBlock || existingBlock instanceof AirBlock || existingBlock instanceof LiquidBlock || existingBlock == Blocks.BEDROCK || existingBlock == Blocks.COBBLESTONE) {
                        to.setBlock(pos, from.getBlockState(pos), Block.UPDATE_ALL);
                        BlockEntity fromBlockEntity = from.getBlockEntity(pos);
                        BlockEntity toBlockEntity = to.getBlockEntity(pos);
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
     * @param from The level to teleport entities from
     * @param to The level to teleport entities to
     * @param chunkPos The chunk to teleport entities between
     */
    private static void copyEntities(ServerLevel from, ServerLevel to, ChunkPos chunkPos) {
        List<Entity> entities = from.getEntities((Entity) null, new AABB(chunkPos.getMinBlockX(), from.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), from.getMaxBuildHeight(), chunkPos.getMaxBlockZ()), (x) -> true);
        for (Entity e : entities) {
            e.changeDimension(to, ENTITY_TELEPORT);
        }
    }

    /**
     * Generates a Bedrock chest containing a chunk spawner at the bottom of a chunk
     * @param targetLevel The level of the chunk
     * @param chunkPos The position of the chunk
     */
    private static void createNextSpawner(ServerLevel targetLevel, ChunkPos chunkPos) {
        BlockPos blockPos = new BlockPos(chunkPos.getMiddleBlockX(), targetLevel.getMinBuildHeight() + 4, chunkPos.getMiddleBlockZ());
        targetLevel.setBlock(blockPos, OneChunkMod.BEDROCK_CHEST_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
        if (targetLevel.getBlockEntity(blockPos) instanceof BedrockChestBlockEntity chestEntity) {
            chestEntity.setItem(0, new ItemStack(OneChunkMod.SPAWN_CHUNK_BLOCK_ITEM.get(), 1));
        }
    }

    /**
     * A generic teleporter that copies entities from one dimension to another, maintaining position.
     */
    private static class EntityTeleport implements ITeleporter {
        @Override
        public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo)
        {
            return new PortalInfo(entity.position(), Vec3.ZERO, entity.xRotO, entity.yRotO);
        }

        @Override
        public boolean isVanilla()
        {
            return false;
        }

        @Override
        public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld)
        {
            return false;
        }
    };
}
