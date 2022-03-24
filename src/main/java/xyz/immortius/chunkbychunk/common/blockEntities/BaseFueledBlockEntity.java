package xyz.immortius.chunkbychunk.common.blockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.interop.SidedBlockEntityInteropBase;

import java.util.Map;

/**
 * Abstract base for any BlockEntity that consume fuel. Handles item storage and fuel tracking - provided methods to handle
 * fuel updates
 */
public abstract class BaseFueledBlockEntity extends SidedBlockEntityInteropBase {

    private final int fuelSlot;
    private final Map<Item, FuelValueSupplier> itemFuel;
    private final Map<TagKey<Item>, FuelValueSupplier> tagFuel;
    private int remainingFuel;
    private int chargedFuel;

    private NonNullList<ItemStack> items;

    protected BaseFueledBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int numItemSlots, int fuelSlot, Map<Item, FuelValueSupplier> itemFuel, Map<TagKey<Item>, FuelValueSupplier> tagFuel) {
        super(blockEntityType, pos, state);
        this.items = NonNullList.withSize(numItemSlots, ItemStack.EMPTY);
        this.fuelSlot = fuelSlot;
        this.itemFuel = itemFuel;
        this.tagFuel = tagFuel;
    }

    /**
     * @return The amount of fuel provided by the last consumed fuel item
     */
    public int getChargedFuel() {
        return chargedFuel;
    }

    /**
     * @return The amount of remaining fuel
     */
    public int getRemainingFuel() {
        return remainingFuel;
    }

    public void setRemainingFuel(int value) {
        this.remainingFuel = value;
    }

    /**
     * Consumes available fuel up to the given amount. This won't go below 0
     * @param amount The amount to consume up to
     * @return The amount of fuel actually consumed
     */
    protected int consumeFuel(int amount) {
        int consumed = Math.min(amount, remainingFuel);
        remainingFuel -= consumed;
        return consumed;
    }

    /**
     * If the block entity is out of fuel and a fuel item is available, consumes it
     * @return Whether an item was consumed to produce fuel
     */
    protected boolean checkConsumeFuelItem() {
        ItemStack fuelItem = items.get(fuelSlot);
        if (remainingFuel == 0 && isFuel(fuelItem)) {
            chargedFuel = remainingFuel = getFuelValue(fuelItem);
            fuelItem.shrink(1);
            return true;
        }
        return false;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.chargedFuel = tag.getInt("ChargedFuel");
        this.remainingFuel = tag.getInt("RemainingFuel");

    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ChargedFuel", this.chargedFuel);
        tag.putInt("RemainingFuel", this.remainingFuel);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    /**
     * @param itemStack
     * @return Is this item fuel
     */
    public boolean isFuel(ItemStack itemStack) {
        if (itemFuel.getOrDefault(itemStack.getItem(), () -> 0).get() > 0) {
            return true;
        }
        for (Map.Entry<TagKey<Item>, FuelValueSupplier> entry : tagFuel.entrySet()) {
            if (itemStack.is(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param itemStack
     * @return How much fuel does this item provide (0 if not fuel)
     */
    public int getFuelValue(ItemStack itemStack) {
        FuelValueSupplier fuelValueSupplier = itemFuel.get(itemStack.getItem());
        if (fuelValueSupplier == null) {
            for (Map.Entry<TagKey<Item>, FuelValueSupplier> entry : tagFuel.entrySet()) {
                if (itemStack.is(entry.getKey())) {
                    fuelValueSupplier = entry.getValue();
                }
            }
        }

        if (fuelValueSupplier != null) {
            return fuelValueSupplier.get();
        }
        return 0;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int split) {
        return ContainerHelper.removeItem(items, slot, split);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack newItem) {
        items.set(slot, newItem);
        if (newItem.getCount() > this.getMaxStackSize()) {
            newItem.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (ItemStack itemstack : items) {
            contents.accountStack(itemstack);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level.getBlockEntity(worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        if (slot == fuelSlot) {
            return isFuel(item);
        }
        return true;
    }

    @FunctionalInterface
    public interface FuelValueSupplier {
        int get();
    }

}
