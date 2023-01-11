package xyz.immortius.chunkbychunk.common.blockEntities;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.dimension.DimensionType;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.blocks.AbstractTriggeredSpawnChunkBlock;
import xyz.immortius.chunkbychunk.common.world.ControllableChunkMap;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Base class for all chunk spawning block entities. These block entities wait a short period so that entities can spawn
 * in the generation dimension before spawning a chunk.
 */
public abstract class AbstractSpawnChunkBlockEntity extends BlockEntity {

    private static final int TICKS_TO_SPAWN_CHUNK = 1;
    private static final int TICKS_TO_SYNCH_CHUNK = 3;
    private static final int TICKS_TO_SPAWN_ENTITIES = 10;

    private final Function<BlockPos, ChunkPos> sourceChunkPosFunc;
    private int tickCounter = 0;

    public AbstractSpawnChunkBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, Function<BlockPos, ChunkPos> sourceChunkPosFunc) {
        super(blockEntityType, pos, state);
        this.sourceChunkPosFunc = sourceChunkPosFunc;
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, AbstractSpawnChunkBlockEntity entity) {
        ServerLevel serverLevel = (ServerLevel) level;
        if (blockState.getBlock() instanceof AbstractTriggeredSpawnChunkBlock spawnBlock) {
            ServerLevel sourceLevel = serverLevel.getServer().getLevel(spawnBlock.getSourceLevel(serverLevel));
            if (sourceLevel.getChunkSource().getPendingTasksCount() == 0) {
                entity.tickCounter++;
            }

            if (!spawnBlock.validForLevel(serverLevel) || sourceLevel == null) {
                serverLevel.setBlock(blockPos, serverLevel.getBlockState(blockPos.north()), Block.UPDATE_NONE);
                return;
            }

            ChunkPos targetChunkPos = new ChunkPos(blockPos);
            ChunkPos sourceChunkPos = entity.sourceChunkPosFunc.apply(blockPos);

            sourceLevel.getChunkSource().getChunk(sourceChunkPos.x, sourceChunkPos.z, true);

            if (entity.tickCounter == TICKS_TO_SPAWN_CHUNK) {
                spawnChunk(sourceLevel, sourceChunkPos, serverLevel, targetChunkPos);
            } else if (entity.tickCounter == TICKS_TO_SYNCH_CHUNK) {
                if (synchChunks(serverLevel, targetChunkPos)) {
                    // Only synch one chunk a tick, so add an extra tick
                    entity.tickCounter--;
                }
            } else if (entity.tickCounter >= TICKS_TO_SPAWN_ENTITIES) {
                SpawnChunkHelper.spawnChunkEntities(serverLevel, targetChunkPos, sourceLevel, entity.sourceChunkPosFunc.apply(blockPos));
                if (serverLevel.getBlockState(blockPos) == blockState) {
                    serverLevel.setBlock(blockPos, serverLevel.getBlockState(blockPos.north()), Block.UPDATE_NONE);
                }
            }
        }
    }

    private static boolean synchChunks(ServerLevel targetLevel, ChunkPos targetChunkPos) {
        if (targetLevel.getChunkSource().getGenerator() instanceof SkyChunkGenerator generator) {
            for (ResourceKey<Level> synchLevelId : generator.getSynchedLevels()) {
                ServerLevel synchLevel = targetLevel.getServer().getLevel(synchLevelId);
                double scale = DimensionType.getTeleportationScale(targetLevel.dimensionType(), synchLevel.dimensionType());
                BlockPos pos = targetChunkPos.getMiddleBlockPosition(0);
                ChunkPos synchChunk = new ChunkPos(new BlockPos(pos.getX() * scale, 0, pos.getZ() * scale));
                if (SpawnChunkHelper.isEmptyChunk(synchLevel, synchChunk) && !(synchLevel.getBlockState(synchChunk.getMiddleBlockPosition(synchLevel.getMaxBuildHeight() - 1)).getBlock() instanceof AbstractTriggeredSpawnChunkBlock)) {
                    BlockPos genBlockPos = synchChunk.getMiddleBlockPosition(synchLevel.getMaxBuildHeight() - 1);
                    synchLevel.setBlock(genBlockPos, Services.PLATFORM.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_NONE);
                    return true;
                }
            }
        }
        return false;
    }

    private static void spawnChunk(ServerLevel sourceLevel, ChunkPos sourceChunkPos, ServerLevel targetLevel, ChunkPos targetChunkPos) {
        if (SpawnChunkHelper.isEmptyChunk(targetLevel, targetChunkPos)) {
            ChunkAccess sourceChunk = sourceLevel.getChunk(sourceChunkPos.x, sourceChunkPos.z);
            ChunkAccess targetChunk = targetLevel.getChunk(targetChunkPos.x, targetChunkPos.z);
            if (sourceChunk.getSections().length != targetChunk.getSections().length) {
                ChunkByChunkConstants.LOGGER.warn("Section count mismatch between {} and {} - {} vs {}", sourceLevel.dimension(), targetLevel.dimension(), sourceChunk.getSections().length, targetChunk.getSections().length);
            }

            boolean biomesUpdated = false;
            for (int targetIndex = 0; targetIndex < targetChunk.getSections().length; targetIndex++) {
                int sourceIndex = (targetIndex < sourceChunk.getSections().length) ? targetIndex : sourceChunk.getSections().length - 1;

                PalettedContainer<Holder<Biome>> sourceBiomes = sourceChunk.getSections()[sourceIndex].getBiomes();
                PalettedContainer<Holder<Biome>> targetBiomes = targetChunk.getSections()[targetIndex].getBiomes();

                byte[] buffer = new byte[sourceBiomes.getSerializedSize()];
                FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(buffer));
                friendlyByteBuf.writerIndex(0);
                sourceBiomes.write(friendlyByteBuf);

                byte[] targetBuffer = new byte[targetBiomes.getSerializedSize()];
                FriendlyByteBuf targetFriendlyByteBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(targetBuffer));
                targetFriendlyByteBuf.writerIndex(0);
                targetBiomes.write(targetFriendlyByteBuf);

                if (!Arrays.equals(buffer, targetBuffer)) {
                    friendlyByteBuf.readerIndex(0);
                    targetBiomes.read(friendlyByteBuf);
                    targetChunk.setUnsaved(true);
                    biomesUpdated = true;
                }
            }
            SpawnChunkHelper.spawnChunkBlocks(targetLevel, targetChunkPos, sourceLevel, sourceChunkPos);
            if (biomesUpdated) {
                ChunkByChunkConstants.LOGGER.info("Biomes changed, forcing reload");
                ((ControllableChunkMap) targetLevel.getChunkSource().chunkMap).forceReloadChunk(targetChunkPos);
            }
        }
    }
}
