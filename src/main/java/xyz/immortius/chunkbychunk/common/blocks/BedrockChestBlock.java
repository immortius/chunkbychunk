package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.immortius.chunkbychunk.common.blockEntities.BedrockChestBlockEntity;
import xyz.immortius.chunkbychunk.common.config.ChunkByChunkConfig;

/**
 * A Bedrock Chest is a chest made of Bedrock that can only be opened if the chunk above it is cleared - with
 * a little leeway, and ignoring some types of block.
 */
public class BedrockChestBlock extends BaseEntityBlock {
    public BedrockChestBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BedrockChestBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof BedrockChestBlockEntity bedrockChestBlockEntity) {
                int blockCount = getBlockCount(level, new ChunkPos(pos), pos.getY());
                if (blockCount <= ChunkByChunkConfig.bedrockChestBlocksRemainingThreshold.get()) {
                    player.openMenu(bedrockChestBlockEntity);
                } else {
                    player.displayClientMessage(new TranslatableComponent("ui.chunkbychunk.bedrockchest.sealedmessage", new TextComponent(Integer.toString(blockCount - ChunkByChunkConfig.bedrockChestBlocksRemainingThreshold.get())).withStyle(ChatFormatting.RED)), true);
                }

                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    private static int getBlockCount(Level level, ChunkPos chunkPos, int aboveY) {
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        int count = 0;
        for (int x = chunkPos.getMinBlockX(); x < chunkPos.getMaxBlockX(); x++) {
            for (int y = aboveY + 1; y <= level.getMaxBuildHeight(); y++) {
                for (int z = chunkPos.getMinBlockZ(); z < chunkPos.getMaxBlockZ(); z++) {
                    Block block = chunk.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (!(block instanceof AirBlock) &&
                            !(block instanceof LiquidBlock) &&
                            !(block instanceof LadderBlock) &&
                            !(block instanceof LeavesBlock) &&
                            block != Blocks.GLOW_LICHEN &&
                            block != Blocks.VINE &&
                            !(block instanceof TorchBlock)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

}
