package xyz.immortius.chunkbychunk.common.menus;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Result slot for the world forge, can only remove items from this slot
 */
public class WorldForgeResultSlot extends Slot {
    public WorldForgeResultSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    public boolean mayPlace(ItemStack item) {
        return false;
    }
}
