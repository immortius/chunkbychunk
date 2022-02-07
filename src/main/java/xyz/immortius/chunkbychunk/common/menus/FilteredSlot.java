package xyz.immortius.chunkbychunk.common.menus;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldForgeBlockEntity;

import java.util.function.Predicate;

public class FilteredSlot extends Slot {

    private final Predicate<ItemStack> itemFilter;

    public FilteredSlot(Container container, int slot, int x, int y, Predicate<ItemStack> itemFilter) {
        super(container, slot, x, y);
        this.itemFilter = itemFilter;
    }

    public boolean mayPlace(ItemStack itemStack) {
        return itemFilter.test(itemStack);
    }
}
