package xyz.immortius.chunkbychunk.common.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldScannerBlockEntity;
import xyz.immortius.chunkbychunk.interop.Services;

/**
 * World Scanner block is used to scan unspawned chunks for particular features.
 */
public class WorldScannerBlock extends AbstractContainerBlock {
    public static final MapCodec<WorldScannerBlock> CODEC = simpleCodec(WorldScannerBlock::new);

    public WorldScannerBlock(Properties blockProperties) {
        super(blockProperties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorldScannerBlockEntity(pos, state);
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof WorldScannerBlockEntity) {
            player.openMenu((MenuProvider) blockentity);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return level.isClientSide ? null : createTickerHelper(entityType, Services.PLATFORM.worldScannerEntity(), WorldScannerBlockEntity::serverTick);
    }

}
