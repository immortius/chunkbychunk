package xyz.immortius.chunkbychunk.common.menus;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.common.blockEntities.BedrockChestBlockEntity;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

/**
 * Menu for the bedrock chest. It has a single item slot.
 */
public class BedrockChestMenu extends AbstractContainerMenu {
    private final Container container;

    public BedrockChestMenu(int menuId, Inventory inventory) {
        this(menuId, inventory, new SimpleContainer(BedrockChestBlockEntity.CONTAINER_SIZE));
    }

    public BedrockChestMenu(int menuId, Inventory inventory, Container container) {
        super(ChunkByChunkConstants.bedrockChestMenu(), menuId);
        this.container = container;
        checkContainerSize(container, BedrockChestBlockEntity.CONTAINER_SIZE);
        container.startOpen(inventory.player);

        for(int k = 0; k < BedrockChestBlockEntity.ROWS; ++k) {
            for(int l = 0; l < BedrockChestBlockEntity.COLUMNS; ++l) {
                this.addSlot(new ShulkerBoxSlot(container, 0, 8 + 4 * 18, 18 + 18));
            }
        }

        // Player Inventory
        for(int i1 = 0; i1 < 3; ++i1) {
            for(int k1 = 0; k1 < 9; ++k1) {
                this.addSlot(new Slot(inventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 84 + i1 * 18));
            }
        }

        // Player toolbar
        for(int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(inventory, j1, 8 + j1 * 18, 142));
        }

    }

    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(itemstack1, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
