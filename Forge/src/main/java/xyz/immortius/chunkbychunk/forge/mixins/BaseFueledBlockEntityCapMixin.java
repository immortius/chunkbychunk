package xyz.immortius.chunkbychunk.forge.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import xyz.immortius.chunkbychunk.common.blockEntities.BaseFueledBlockEntity;

/**
 * Inject ITEM_HANDLER_CAPABILITY required in forge for item flow to be handled correctly.
 */
@Mixin(BaseFueledBlockEntity.class)
public abstract class BaseFueledBlockEntityCapMixin extends BaseContainerBlockEntity implements WorldlyContainer, StackedContentsCompatible {
    protected BaseFueledBlockEntityCapMixin(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    net.minecraftforge.common.util.LazyOptional<? extends net.minecraftforge.items.IItemHandler>[] handlers = initialiseHandlers();

    private net.minecraftforge.common.util.LazyOptional<? extends net.minecraftforge.items.IItemHandler>[] initialiseHandlers() {
        return net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);
    }

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @javax.annotation.Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == Direction.UP)
                return handlers[0].cast();
            else if (facing == Direction.DOWN)
                return handlers[1].cast();
            else
                return handlers[2].cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<? extends IItemHandler> handler : handlers) {
            handler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.handlers = net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);
    }
}
