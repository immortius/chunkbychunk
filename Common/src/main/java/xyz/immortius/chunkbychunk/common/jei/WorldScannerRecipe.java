package xyz.immortius.chunkbychunk.common.jei;

import net.minecraft.world.item.ItemStack;

public class WorldScannerRecipe {
    private final ItemStack item;
    private final int value;

    public WorldScannerRecipe(ItemStack item, int value) {
        this.item = item;
        this.value = value;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getValue() {
        return value;
    }
}
