package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This is the base for blocks that can be used to trigger spawning an "empty" chunk. Empty in this case is signified by the chunk not having bedrock at the base level.
 *
 * When used these blocks will try to spawn the first valid chunk out of:
 * <ul>
 *     <li>The chunk they are in</li>
 *     <li>The chunk in the direction the player is looking in, if the block is on a chunk border</li>
 *     <li>Any other directly adjacent chunk</li>
 * </ul>
 */
public abstract class BaseSpawnChunkBlock extends Block {

    private static final EnumSet<Direction> HORIZONTAL_DIR = EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    public BaseSpawnChunkBlock(BlockBehaviour.Properties blockProperties) {
        super(blockProperties);
    }

    public abstract BlockState getTriggeredBlockState();

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (Level.OVERWORLD.equals(context.getLevel().dimension())) {
            return super.getStateForPlacement(context);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel) {
            if (Level.OVERWORLD.equals(level.dimension())) {
                List<BlockPos> targetPositions = new ArrayList<>();
                BlockPos initialPos = pos.atY(level.getMaxBuildHeight() - 1);
                targetPositions.add(initialPos);
                Direction targetDirection = hit.getDirection();
                if (!HORIZONTAL_DIR.contains(targetDirection)) {
                    targetDirection = Direction.NORTH;
                }
                targetPositions.add(initialPos.relative(targetDirection.getOpposite()));
                targetPositions.add(initialPos.relative(targetDirection.getCounterClockWise()));
                targetPositions.add(initialPos.relative(targetDirection.getClockWise()));
                targetPositions.add(initialPos.relative(targetDirection));

                for (BlockPos targetPos : targetPositions) {
                    ChunkPos targetChunkPos = new ChunkPos(targetPos);
                    if (SpawnChunkHelper.isValidForChunkSpawn(serverLevel) && SpawnChunkHelper.isEmptyChunk(serverLevel, targetChunkPos)) {
                        serverLevel.setBlock(targetPos, getTriggeredBlockState(), Block.UPDATE_ALL);
                        if (!pos.equals(targetPos)) {
                            serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                        level.playSound(null, pos, ChunkByChunkConstants.spawnChunkSoundEffect(), SoundSource.BLOCKS, 1.0f, 1.0f);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
}
