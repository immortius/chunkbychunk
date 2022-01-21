package xyz.immortius.chunkbychunk.common.menus;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

/**
 * Slot for taking fuel for the world forge
 */
public class WorldForgeFuelSlot extends Slot {
    public WorldForgeFuelSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }
}
