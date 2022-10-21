package xyz.immortius.chunkbychunk.common.menus;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * BaseContainerMenu is a base for any menu that includes access to the player's inventory in the standard layout
 */
public abstract class BaseInventoryContainerMenu extends AbstractContainerMenu {
    public static final int INVENTORY_ROWS = 3;
    public static final int INVENTORY_COLUMNS = 9;
    public static final int NUM_QUICK_SLOTS = INVENTORY_COLUMNS;
    public static final int TOTAL_INVENTORY_SLOTS = INVENTORY_ROWS * INVENTORY_COLUMNS + NUM_QUICK_SLOTS;
    public static final int INVENTORY_SLOT_PIXELS = 18;
    private static final int QUICK_SLOT_Y_OFFSET = INVENTORY_ROWS * INVENTORY_SLOT_PIXELS + 4;

    private final Container container;

    /**
     * @param menuType The type of the menu
     * @param menuId The id of the menu
     * @param container The container the player is opening
     * @param inventory The player's inventory
     * @param playerInventoryX The x-coord of the top-left corner of the player's inventory display
     * @param playerInventoryY The y-coord of the top-left corner of the player's inventory display
     */
    protected BaseInventoryContainerMenu(MenuType<?> menuType, int menuId, Container container, Inventory inventory, int playerInventoryX, int playerInventoryY) {
        super(menuType, menuId);
        this.container = container;
        container.startOpen(inventory.player);

        // Player Inventory
        for(int slotY = 0; slotY < INVENTORY_ROWS; ++slotY) {
            for(int slotX = 0; slotX < INVENTORY_COLUMNS; ++slotX) {
                this.addSlot(new Slot(inventory, slotX + slotY * INVENTORY_COLUMNS + NUM_QUICK_SLOTS, playerInventoryX + slotX * INVENTORY_SLOT_PIXELS, playerInventoryY + slotY * INVENTORY_SLOT_PIXELS));
            }
        }

        // Player toolbar
        for(int slot = 0; slot < NUM_QUICK_SLOTS; ++slot) {
            this.addSlot(new Slot(inventory, slot, playerInventoryX + slot * INVENTORY_SLOT_PIXELS, playerInventoryY + QUICK_SLOT_Y_OFFSET));
        }
    }

    public Container getContainer() {
        return container;
    }

    public Slot getContainerSlot(int slot) {
        return this.slots.get(slot + TOTAL_INVENTORY_SLOTS);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack intermediateStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack sourceStack = slot.getItem();
            intermediateStack = sourceStack.copy();
            if (index < TOTAL_INVENTORY_SLOTS) {
                if (!quickMoveToContainer(sourceStack)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(sourceStack, 0, TOTAL_INVENTORY_SLOTS, true)) {
                return ItemStack.EMPTY;
            }

            if (sourceStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return intermediateStack;
    }

    protected boolean quickMoveToContainer(ItemStack stack) {
        if (this.container.getContainerSize() > 0) {
            return this.moveItemStackToContainerSlot(stack, 0, this.container.getContainerSize(), false);
        }
        return false;
    }

    protected final boolean moveItemStackToContainerSlot(ItemStack stack, int firstSlot, int lastSlot, boolean reverseOrder) {
        return this.moveItemStackTo(stack, firstSlot + TOTAL_INVENTORY_SLOTS, lastSlot + TOTAL_INVENTORY_SLOTS, reverseOrder);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
