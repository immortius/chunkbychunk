package xyz.immortius.chunkbychunk.common.menus;

import com.google.common.base.Preconditions;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldForgeBlockEntity;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

public class WorldForgeMenu extends AbstractContainerMenu {
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;
    private static final int NUM_QUICK_SLOTS = INVENTORY_COLUMNS;
    private static final int NUM_INVENTORY_SLOTS = INVENTORY_ROWS * INVENTORY_COLUMNS;
    private static final int FIRST_INVENTORY_SLOT = WorldForgeBlockEntity.NUM_ITEM_SLOTS;
    private static final int FIRST_QUICK_SLOT = WorldForgeBlockEntity.NUM_ITEM_SLOTS + NUM_INVENTORY_SLOTS;
    private static final int TOTAL_SLOTS = FIRST_QUICK_SLOT + NUM_QUICK_SLOTS;

    private final Container container;
    private final ContainerData containerData;

    public WorldForgeMenu(int menuId, Inventory inventory) {
        this(menuId, inventory, new SimpleContainer(WorldForgeBlockEntity.NUM_ITEM_SLOTS), new SimpleContainerData(WorldForgeBlockEntity.NUM_DATA_ITEMS));
    }

    public WorldForgeMenu(int menuId, Inventory inventory, Container container, ContainerData containerData) {
        super(ChunkByChunkConstants.worldForgeMenu(), menuId);
        this.container = container;
        this.containerData = containerData;
        Preconditions.checkArgument(container.getContainerSize() >= WorldForgeBlockEntity.NUM_ITEM_SLOTS, "Expected " + WorldForgeBlockEntity.NUM_ITEM_SLOTS + " item slots, but entity has " + container.getContainerSize());
        Preconditions.checkArgument(containerData.getCount() >= WorldForgeBlockEntity.NUM_DATA_ITEMS, "Expected " + WorldForgeBlockEntity.NUM_DATA_ITEMS + " data items, but entity has " + containerData.getCount());
        container.startOpen(inventory.player);

        addSlot(new WorldForgeFuelSlot(container, WorldForgeBlockEntity.SLOT_INPUT, 58, 35));
        addSlot(new WorldForgeResultSlot(container, WorldForgeBlockEntity.SLOT_RESULT, 116, 35));

        // Player Inventory
        for(int i1 = 0; i1 < INVENTORY_ROWS; ++i1) {
            for(int k1 = 0; k1 < INVENTORY_COLUMNS; ++k1) {
                this.addSlot(new Slot(inventory, k1 + i1 * INVENTORY_COLUMNS + 9, 8 + k1 * 18, 84 + i1 * 18));
            }
        }

        // Player toolbar
        for(int j1 = 0; j1 < NUM_QUICK_SLOTS; ++j1) {
            this.addSlot(new Slot(inventory, j1, 8 + j1 * 18, 142));
        }

        addDataSlots(containerData);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    public int getProgress() {
        return this.containerData.get(WorldForgeBlockEntity.DATA_PROGRESS);
    }

    public int getGoal() {
        return this.containerData.get(WorldForgeBlockEntity.DATA_GOAL);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Move result to player's inventory
            if (slotIndex == WorldForgeBlockEntity.SLOT_RESULT) {
                if (!this.moveItemStackTo(itemstack1, FIRST_INVENTORY_SLOT, TOTAL_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            // Move item from player's inventory into forge
            } else if (slotIndex != WorldForgeBlockEntity.SLOT_INPUT) {
                if (WorldForgeBlockEntity.isFuel(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, WorldForgeBlockEntity.SLOT_INPUT,  WorldForgeBlockEntity.SLOT_INPUT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex >= FIRST_INVENTORY_SLOT && slotIndex < FIRST_QUICK_SLOT) {
                    if (!this.moveItemStackTo(itemstack1, FIRST_QUICK_SLOT, TOTAL_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex >= FIRST_QUICK_SLOT && slotIndex < TOTAL_SLOTS && !this.moveItemStackTo(itemstack1, FIRST_INVENTORY_SLOT, FIRST_QUICK_SLOT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, FIRST_INVENTORY_SLOT, TOTAL_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}
