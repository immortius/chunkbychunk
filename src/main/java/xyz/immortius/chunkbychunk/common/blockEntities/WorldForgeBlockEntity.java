package xyz.immortius.chunkbychunk.common.blockEntities;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.immortius.chunkbychunk.common.menus.WorldForgeMenu;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkSettings;
import xyz.immortius.chunkbychunk.interop.WorldForgeBlockEntityInteropBase;

import java.util.Map;

/**
 * World Forge Block Entity - this holds the input and output of the world forge,
 * and handles dissolving the input to crystalise the output
 */
public class WorldForgeBlockEntity extends WorldForgeBlockEntityInteropBase {
    public static final int NUM_ITEM_SLOTS = 2;
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_RESULT = 1;

    public static final int NUM_DATA_ITEMS = 2;
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_GOAL = 1;

    private static final int GROW_CRYSTAL_AT = 4;
    private static final Map<Item, Integer> FUEL;
    private static final Map<Item, Integer> CRYSTAL_COSTS;
    private static final Item INITIAL_CRYSTAL = ChunkByChunkConstants.worldFragmentItem();
    private static final Map<Item, Item> CRYSTAL_STEPS;

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_RESULT};

    private NonNullList<ItemStack> items = NonNullList.withSize(NUM_ITEM_SLOTS, ItemStack.EMPTY);
    private int progress;
    private int goal;
    private int availableFuel;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int id) {
            switch (id) {
                case 0:
                    return progress;
                case 1:
                    return goal;
                default:
                    return 0;
            }
        }

        public void set(int id, int value) {
            switch (id) {
                case 0:
                    progress = value;
                    break;
                case 1:
                    goal = value;
                    break;
            }

        }

        public int getCount() {
            return NUM_DATA_ITEMS;
        }
    };

    static {
        ImmutableMap.Builder<Item, Integer> fuelBuilder = ImmutableMap.builder();
        for (Item value : ItemTags.DIRT.getValues()) {
            fuelBuilder.put(value, ChunkByChunkSettings.worldForgeSoilFuelValue());
        }
        for (Item value : ItemTags.SAND.getValues()) {
            fuelBuilder.put(value, ChunkByChunkSettings.worldForgeSoilFuelValue());
        }
        fuelBuilder.put(Blocks.GRAVEL.asItem(), ChunkByChunkSettings.worldForgeSoilFuelValue());

        for (Item value : ItemTags.STONE_CRAFTING_MATERIALS.getValues()) {
            fuelBuilder.put(value, ChunkByChunkSettings.worldForgeStoneFuelValue());
        }
        fuelBuilder.put(Blocks.ANDESITE.asItem(), ChunkByChunkSettings.worldForgeStoneFuelValue());
        fuelBuilder.put(Blocks.DIORITE.asItem(), ChunkByChunkSettings.worldForgeStoneFuelValue());
        fuelBuilder.put(Blocks.GRANITE.asItem(), ChunkByChunkSettings.worldForgeStoneFuelValue());

        fuelBuilder.put(ChunkByChunkConstants.worldFragmentItem(), ChunkByChunkSettings.worldForgeFuelPerFragment());
        fuelBuilder.put(ChunkByChunkConstants.worldShardItem(), ChunkByChunkSettings.worldForgeFuelPerFragment() * 4);
        fuelBuilder.put(ChunkByChunkConstants.worldCrystalItem(), ChunkByChunkSettings.worldForgeFuelPerFragment() * 16);

        FUEL = fuelBuilder.build();

        CRYSTAL_COSTS = ImmutableMap.<Item, Integer>builder()
                .put(ChunkByChunkConstants.worldFragmentItem(), ChunkByChunkSettings.worldForgeFuelPerFragment())
                .put(ChunkByChunkConstants.worldShardItem(), ChunkByChunkSettings.worldForgeFuelPerFragment() * 4)
                .put(ChunkByChunkConstants.worldCrystalItem(), ChunkByChunkSettings.worldForgeFuelPerFragment() * 16)
                .put(ChunkByChunkConstants.worldCoreBlockItem(), ChunkByChunkSettings.worldForgeFuelPerFragment() * 64).build();

        CRYSTAL_STEPS = ImmutableMap.<Item, Item>builder()
                .put(ChunkByChunkConstants.worldFragmentItem(), ChunkByChunkConstants.worldShardItem())
                .put(ChunkByChunkConstants.worldShardItem(), ChunkByChunkConstants.worldCrystalItem())
                .put(ChunkByChunkConstants.worldCrystalItem(), ChunkByChunkConstants.worldCrystalItem()).build();
    }

    public WorldForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ChunkByChunkConstants.worldForgeEntity(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.chunkbychunk.worldforge");
    }

    @Override
    protected AbstractContainerMenu createMenu(int menuId, Inventory inventory) {
        return new WorldForgeMenu(menuId, inventory, this, this.dataAccess);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.progress = tag.getInt("Progress");
        this.availableFuel = tag.getInt("AvailableFuel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", this.progress);
        tag.putInt("AvailableFuel", this.availableFuel);

        ContainerHelper.saveAllItems(tag, this.items);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, WorldForgeBlockEntity entity) {
        ItemStack inputItems = entity.items.get(SLOT_INPUT);
        ItemStack outputItems = entity.items.get(SLOT_RESULT);

        // Consume available fuel
        if (entity.availableFuel > 0) {
            int consumeAmount = Math.min(entity.availableFuel, ChunkByChunkSettings.worldForgeProductionRate());
            entity.availableFuel -= consumeAmount;
            entity.progress += consumeAmount;
        }

        // Determine what crystal we're making and its cost
        Item producingItem;
        if (outputItems.isEmpty()) {
            producingItem = INITIAL_CRYSTAL;
        } else {
            producingItem = outputItems.getItem();
        }

        // Check if we've reached the finished result or otherwise blocked
        if (outputItems.getCount() == outputItems.getMaxStackSize()) {
            return;
        }

        // Check to consume next item
        if (entity.availableFuel == 0 && isFuel(inputItems)) {
            entity.availableFuel += getFuelValue(inputItems);
            inputItems.shrink(1);
        }

        int itemCost = CRYSTAL_COSTS.get(producingItem);
        Item nextItem = CRYSTAL_STEPS.get(producingItem);
        entity.goal = itemCost;

        // Produce the item if we can, replacing the existing item
        if (entity.progress >= itemCost) {
            entity.progress -= itemCost;
            if (outputItems.isEmpty()) {
                entity.setItem(SLOT_RESULT, producingItem.getDefaultInstance());
            } else if (outputItems.getCount() == GROW_CRYSTAL_AT - 1 && nextItem != null) {
                entity.setItem(SLOT_RESULT, nextItem.getDefaultInstance());
                entity.goal = CRYSTAL_COSTS.get(nextItem);
            } else {
                outputItems.grow(1);
            }
        }
    }

    public static boolean isFuel(ItemStack itemStack) {
        return FUEL.getOrDefault(itemStack.getItem(), 0) > 0;
    }

    public static int getFuelValue(ItemStack itemStack) {
        return FUEL.getOrDefault(itemStack.getItem(), 0);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        switch (direction) {
            case UP:
                return SLOTS_FOR_UP;
            default:
                return SLOTS_FOR_DOWN;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction p_19237_) {
        return canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return slot == SLOT_RESULT;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
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
    public boolean stillValid(Player player) {
        if (level.getBlockEntity(worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
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
    public boolean canPlaceItem(int slot, ItemStack item) {
        if (slot == SLOT_RESULT) {
            return false;
        } else {
            return isFuel(item);
        }
    }

}
