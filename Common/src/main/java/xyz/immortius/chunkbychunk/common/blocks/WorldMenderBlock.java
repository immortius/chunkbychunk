package xyz.immortius.chunkbychunk.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldMenderBlockEntity;
import xyz.immortius.chunkbychunk.interop.Services;

/**
 * World Mender block is used to auto-spawned chunks.
 */
public class WorldMenderBlock extends AbstractContainerBlock {
    public WorldMenderBlock(Properties blockProperties) {
        super(blockProperties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorldMenderBlockEntity(pos, state);
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof WorldMenderBlockEntity) {
            player.openMenu((MenuProvider) blockentity);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return level.isClientSide ? null : createTickerHelper(entityType, Services.PLATFORM.worldMenderEntity(), WorldMenderBlockEntity::serverTick);
    }

}
