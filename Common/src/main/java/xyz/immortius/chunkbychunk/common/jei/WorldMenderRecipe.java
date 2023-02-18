package xyz.immortius.chunkbychunk.common.jei;

import net.minecraft.world.item.ItemStack;

public class WorldMenderRecipe {
    private final ItemStack input;

    public WorldMenderRecipe(ItemStack input) {
        this.input = input;
    }

    public ItemStack getInput() {
        return input;
    }
}
