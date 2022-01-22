package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.util.*;

/**
 * This is the base for blocks that can be used to trigger spawning an "empty" chunk. Empty in this case is signified by the chunk not having bedrock at the base level.
 */
public abstract class BaseSpawnChunkBlock extends Block {

    private static final EnumSet<Direction> HORIZONTAL_DIR = EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    public BaseSpawnChunkBlock(BlockBehaviour.Properties blockProperties) {
        super(blockProperties);
    }

    protected abstract ChunkPos getSourceChunk(Level targetLevel, BlockPos targetBlockPos);

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState prevState, boolean p_60570_) {
        super.onPlace(state, level, pos, prevState, p_60570_);
        if (!level.isClientSide()) {
            ServerLevel targetLevel = (ServerLevel) level;
            ServerLevel sourceLevel = level.getServer().getLevel(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL);
            if (sourceLevel != null && SpawnChunkHelper.isValidForChunkSpawn(targetLevel)) {
                Set<BlockPos> relativePos = getPossibleTargetPos(targetLevel, pos);
                for (BlockPos targetPos : relativePos) {
                    ChunkPos sourceChunkPos = getSourceChunk(level, targetPos);
                    sourceLevel.setChunkForced(sourceChunkPos.x, sourceChunkPos.z, true);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState prevState, boolean p_60519_) {
        super.onRemove(state, level, pos, prevState, p_60519_);
        if (!level.isClientSide()) {
            ServerLevel targetLevel = (ServerLevel) level;
            ServerLevel sourceLevel = level.getServer().getLevel(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL);
            if (sourceLevel != null && SpawnChunkHelper.isValidForChunkSpawn(targetLevel)) {
                Set<BlockPos> relativePos = getPossibleTargetPos(targetLevel, pos);
                for (BlockPos targetPos : relativePos) {
                    ChunkPos sourceChunkPos = getSourceChunk(level, targetPos);
                    sourceLevel.setChunkForced(sourceChunkPos.x, sourceChunkPos.z, false);
                }
            }
        }
    }

    private Set<BlockPos> getPossibleTargetPos(ServerLevel level, BlockPos pos) {
        ChunkPos originChunk = new ChunkPos(pos);
        if (SpawnChunkHelper.isEmptyChunk(level, originChunk)) {
            return Collections.singleton(pos);
        }
        Set<BlockPos> result = new LinkedHashSet<>();
        Set<ChunkPos> coveredChunks = new HashSet<>();
        for (Direction dir : HORIZONTAL_DIR) {
            ChunkPos adjChunk = new ChunkPos(pos.relative(dir));
            if (coveredChunks.add(adjChunk) && SpawnChunkHelper.isEmptyChunk(level, adjChunk)) {
                result.add(pos.relative(dir));
            }
        }
        return result;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel) {
            List<BlockPos> targetPositions = new ArrayList<>();
            targetPositions.add(pos);
            if (HORIZONTAL_DIR.contains(hit.getDirection())) {
                targetPositions.add(pos.relative(hit.getDirection().getOpposite()));
                targetPositions.add(pos.relative(hit.getDirection().getCounterClockWise()));
                targetPositions.add(pos.relative(hit.getDirection().getClockWise()));
                targetPositions.add(pos.relative(hit.getDirection()));
            }

            for (BlockPos targetPos : targetPositions) {
                ChunkPos targetChunkPos = new ChunkPos(targetPos);
                if (SpawnChunkHelper.isValidForChunkSpawn(serverLevel) && SpawnChunkHelper.isEmptyChunk(serverLevel, targetChunkPos)) {
                    ChunkPos sourceChunkPos = getSourceChunk(level, targetPos);
                    serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    level.playSound(null, pos, ChunkByChunkConstants.spawnChunkSoundEffect(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    SpawnChunkHelper.spawnChunk(serverLevel, sourceChunkPos, targetChunkPos);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
