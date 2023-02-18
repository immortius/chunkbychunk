package xyz.immortius.chunkbychunk.common.jei;

import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.List;

public class WorldForgeRecipe {

    private final int fuelValue;
    private final List<ItemStack> inputItems;
    private final ItemStack output;

    public WorldForgeRecipe(List<ItemStack> inputs, int value) {
        this(inputs, value, Services.PLATFORM.worldFragmentItem().getDefaultInstance());
    }

    public WorldForgeRecipe(List<ItemStack> inputItems, int fuelValue, ItemStack outputItem) {
        this.inputItems = inputItems;
        this.fuelValue = fuelValue;
        this.output = outputItem;
    }

    public int getFuelValue() {
        return fuelValue;
    }

    public List<ItemStack> getInputItems() {
        return inputItems;
    }

    public ItemStack getOutput() {
        return output;
    }
}
