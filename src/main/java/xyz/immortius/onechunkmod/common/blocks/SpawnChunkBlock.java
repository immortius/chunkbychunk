package xyz.immortius.onechunkmod.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xyz.immortius.onechunkmod.OneChunkMod;
import xyz.immortius.onechunkmod.common.world.SpawnChunkHelper;

import java.util.Objects;

/**
 * This is a block that can be used to trigger spawning an "empty" chunk. Empty in this case is signified by the chunk not having bedrock at the base level.
 */
public class SpawnChunkBlock extends Block {

    public SpawnChunkBlock(Properties blockProperties) {
        super(blockProperties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState prevState, boolean p_60570_) {
        super.onPlace(state, level, pos, prevState, p_60570_);
        if (!level.isClientSide()) {
            ServerLevel sourceLevel = Objects.requireNonNull(level.getServer()).getLevel(OneChunkMod.SKY_CHUNK_GENERATION_LEVEL);
            if (sourceLevel != null) {
                ChunkPos chunkPos = new ChunkPos(pos);
                sourceLevel.setChunkForced(chunkPos.x, chunkPos.z, true);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState prevState, boolean p_60519_) {
        super.onRemove(state, level, pos, prevState, p_60519_);
        if (!level.isClientSide()) {
            ServerLevel sourceLevel = Objects.requireNonNull(level.getServer()).getLevel(OneChunkMod.SKY_CHUNK_GENERATION_LEVEL);
            if (sourceLevel != null) {
                ChunkPos chunkPos = new ChunkPos(pos);
                sourceLevel.setChunkForced(chunkPos.x, chunkPos.z, false);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel) {
            ChunkPos chunkPos = new ChunkPos(pos);
            if (SpawnChunkHelper.isValidForChunkSpawn(serverLevel) && SpawnChunkHelper.isEmptyChunk(serverLevel, chunkPos)) {
                serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                level.playSound(null, pos, OneChunkMod.SPAWN_CHUNK_SOUND_EVENT.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                SpawnChunkHelper.spawnChunk(serverLevel, chunkPos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

}
