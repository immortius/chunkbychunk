package xyz.immortius.chunkbychunk.common.blockEntities;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
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
import xyz.immortius.chunkbychunk.common.blocks.AbstractTriggeredSpawnChunkBlock;
import xyz.immortius.chunkbychunk.common.world.ControllableChunkMap;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.function.Function;

/**
 * Base class for all chunk spawning block entities. These block entities wait a short period so that entities can spawn
 * in the generation dimension before spawning a chunk.
 */
public abstract class AbstractSpawnChunkBlockEntity extends BlockEntity {

    private static final int TICKS_TO_SPAWN_CHUNK = 1;
    private static final int TICKS_TO_SPAWN_NETHER_CHUNK = 2;
    private static final int TICKS_TO_SPAWN_ENTITIES = 20;

    private final Function<BlockPos, ChunkPos> sourceChunkPosFunc;
    private int tickCounter = 0;

    public AbstractSpawnChunkBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, Function<BlockPos, ChunkPos> sourceChunkPosFunc) {
        super(blockEntityType, pos, state);
        this.sourceChunkPosFunc = sourceChunkPosFunc;
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, AbstractSpawnChunkBlockEntity entity) {
        ServerLevel serverLevel = (ServerLevel) level;
        // If there are no players, entities won't spawn. So don't tick.
        if (serverLevel.getPlayers((p) -> true).isEmpty()) {
            return;
        }
        if (blockState.getBlock() instanceof AbstractTriggeredSpawnChunkBlock spawnBlock) {
            entity.tickCounter++;
            ChunkPos targetChunkPos = new ChunkPos(blockPos);
            ServerLevel sourceLevel = serverLevel.getServer().getLevel(spawnBlock.getSourceLevel(serverLevel));
            if (!spawnBlock.validForLevel(serverLevel) || sourceLevel == null) {
                serverLevel.setBlock(blockPos, serverLevel.getBlockState(blockPos.north()), Block.UPDATE_ALL);
                return;
            }

            if (entity.tickCounter == TICKS_TO_SPAWN_CHUNK) {
                if (SpawnChunkHelper.isEmptyChunk(serverLevel, targetChunkPos)) {
                    ChunkAccess sourceChunk = sourceLevel.getChunk(targetChunkPos.x, targetChunkPos.z);
                    ChunkAccess targetChunk = serverLevel.getChunk(targetChunkPos.x, targetChunkPos.z);
                    for (int i = 0; i < sourceChunk.getSections().length; i++) {
                        if (sourceChunk.getSections()[i].getBiomes() instanceof PalettedContainer<Holder<Biome>> sourceBiomes && targetChunk.getSections()[i].getBiomes() instanceof PalettedContainer<Holder<Biome>> targetBiomes) {
                            byte[] buffer = new byte[sourceBiomes.getSerializedSize()];
                            FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(buffer));
                            friendlyByteBuf.writerIndex(0);
                            sourceBiomes.write(friendlyByteBuf);
                            friendlyByteBuf.readerIndex(0);
                            targetBiomes.read(friendlyByteBuf);
                            targetChunk.setUnsaved(true);
                        }
                    }
                    SpawnChunkHelper.spawnChunkBlocks(serverLevel, targetChunkPos, sourceLevel, entity.sourceChunkPosFunc.apply(blockPos));
                    ((ControllableChunkMap) serverLevel.getChunkSource().chunkMap).forceReloadChunk(targetChunkPos);
                }
            } else if (entity.tickCounter == TICKS_TO_SPAWN_NETHER_CHUNK && ChunkByChunkConfig.get().getGeneration().isSynchNether() && Level.OVERWORLD.equals(serverLevel.dimension())) {
                ServerLevel netherLevel = serverLevel.getServer().getLevel(Level.NETHER);
                double teleportationScale = DimensionType.getTeleportationScale(serverLevel.dimensionType(), netherLevel.dimensionType());
                BlockPos pos = targetChunkPos.getMiddleBlockPosition(0);

                ChunkPos netherChunkPos = new ChunkPos(new BlockPos(pos.getX() * teleportationScale, 0, pos.getZ() * teleportationScale));
                if (SpawnChunkHelper.isEmptyChunk(netherLevel, netherChunkPos)) {
                    SpawnChunkHelper.spawnChunkBlocks(netherLevel, netherChunkPos, netherChunkPos);
                    BlockPos genBlockPos = netherChunkPos.getMiddleBlockPosition(netherLevel.getMaxBuildHeight() - 1);
                    netherLevel.setBlock(genBlockPos, Services.PLATFORM.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_ALL);
                }
            } else if (entity.tickCounter >= TICKS_TO_SPAWN_ENTITIES) {
                SpawnChunkHelper.spawnChunkEntities(serverLevel, targetChunkPos, sourceLevel, entity.sourceChunkPosFunc.apply(blockPos));
                if (serverLevel.getBlockState(blockPos) == blockState) {
                    serverLevel.setBlock(blockPos, serverLevel.getBlockState(blockPos.north()), Block.UPDATE_ALL);
                }
            }
        }
    }
}
