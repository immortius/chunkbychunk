package xyz.immortius.onechunkmod.common.blockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.onechunkmod.OneChunkMod;
import xyz.immortius.onechunkmod.client.menus.BedrockChestMenu;

public class BedrockChestBlockEntity extends RandomizableContainerBlockEntity {

    public static final int COLUMNS = 1;
    public static final int ROWS = 1;
    public static final int CONTAINER_SIZE = COLUMNS * ROWS;

    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public BedrockChestBlockEntity(BlockPos pos, BlockState state) {
        super(OneChunkMod.BEDROCK_CHEST_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.bedrockchest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int menuId, Inventory inventory) {
        return new BedrockChestMenu(menuId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected void saveAdditional(CompoundTag p_187459_) {
        super.saveAdditional(p_187459_);
        if (!this.trySaveLootTable(p_187459_)) {
            ContainerHelper.saveAllItems(p_187459_, this.items);
        }

    }

    public void load(CompoundTag p_155055_) {
        super.load(p_155055_);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(p_155055_)) {
            ContainerHelper.loadAllItems(p_155055_, this.items);
        }

    }
}
