package xyz.immortius.chunkbychunk.common.blockEntities;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xyz.immortius.chunkbychunk.common.menus.WorldForgeMenu;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.Map;

/**
 * World Forge Block Entity - this holds the input and output of the world forge,
 * and handles dissolving the input to crystalise the output
 */
public class WorldForgeBlockEntity extends BaseFueledBlockEntity {
    public static final int NUM_ITEM_SLOTS = 2;
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_RESULT = 1;

    public static final int NUM_DATA_ITEMS = 2;
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_GOAL = 1;

    public static final int GROW_CRYSTAL_AT = 4;
    public static final Map<Item, FuelValueSupplier> FUEL;
    public static final Map<TagKey<Item>, FuelValueSupplier> FUEL_TAGS;
    private static final Map<Item, FuelValueSupplier> CRYSTAL_COSTS;
    private static final Item INITIAL_CRYSTAL = Services.PLATFORM.worldFragmentItem();
    public static final Map<Item, Item> CRYSTAL_STEPS;

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_RESULT};

    private static final TagKey<Item> SOIL_FUEL_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("chunkbychunk:weakworldforgefuel"));
    private static final TagKey<Item> STONE_FUEL_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("chunkbychunk:worldforgefuel"));
    private static final TagKey<Item> STRONG_FUEL_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("chunkbychunk:strongworldforgefuel"));

    private int progress;
    private int goal;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                case DATA_PROGRESS -> progress;
                case DATA_GOAL -> goal;
                default -> 0;
            };
        }

        public void set(int id, int value) {
            switch (id) {
                case DATA_PROGRESS -> progress = value;
                case DATA_GOAL -> goal = value;
            }

        }

        public int getCount() {
            return NUM_DATA_ITEMS;
        }
    };

    static {
        ImmutableMap.Builder<Item, FuelValueSupplier> fuelBuilder = ImmutableMap.builder();

        fuelBuilder.put(Services.PLATFORM.worldFragmentItem(), () -> ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost());
        fuelBuilder.put(Services.PLATFORM.worldShardItem(), () -> ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost() * 4);
        fuelBuilder.put(Services.PLATFORM.worldCrystalItem(), () -> ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost() * 16);

        FUEL = fuelBuilder.build();

        FUEL_TAGS = ImmutableMap.of(SOIL_FUEL_TAG, () -> ChunkByChunkConfig.get().getWorldForge().getSoilFuelValue(),
                STONE_FUEL_TAG, () -> ChunkByChunkConfig.get().getWorldForge().getStoneFuelValue(),
                STRONG_FUEL_TAG, () -> ChunkByChunkConfig.get().getWorldForge().getStrongFuelValue());

        CRYSTAL_COSTS = ImmutableMap.<Item, FuelValueSupplier>builder()
                .put(Services.PLATFORM.worldFragmentItem(),() -> ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost())
                .put(Services.PLATFORM.worldShardItem(), () -> ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost() * 4)
                .put(Services.PLATFORM.worldCrystalItem(), () -> ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost() * 16)
                .put(Services.PLATFORM.worldCoreBlockItem(), () -> ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost() * 64).build();

        CRYSTAL_STEPS = ImmutableMap.<Item, Item>builder()
                .put(Services.PLATFORM.worldFragmentItem(), Services.PLATFORM.worldShardItem())
                .put(Services.PLATFORM.worldShardItem(), Services.PLATFORM.worldCrystalItem())
                .put(Services.PLATFORM.worldCrystalItem(), Services.PLATFORM.worldCoreBlockItem()).build();
    }

    public WorldForgeBlockEntity(BlockPos pos, BlockState state) {
        super(Services.PLATFORM.worldForgeEntity(), pos, state, NUM_ITEM_SLOTS, SLOT_INPUT, FUEL, FUEL_TAGS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.chunkbychunk.worldforge");
    }

    @Override
    protected AbstractContainerMenu createMenu(int menuId, Inventory inventory) {
        return new WorldForgeMenu(menuId, inventory, this, this.dataAccess);
    }

    public static boolean isWorldForgeFuel(ItemStack itemStack) {
        return FUEL.get(itemStack.getItem()) != null || itemStack.is(SOIL_FUEL_TAG) || itemStack.is(STONE_FUEL_TAG) || itemStack.is(STRONG_FUEL_TAG);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.progress = tag.getInt("Progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", this.progress);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, WorldForgeBlockEntity entity) {
        // Consume available fuel
        if (entity.getRemainingFuel() > 0) {
            int consumeAmount = entity.consumeFuel(ChunkByChunkConfig.get().getWorldForge().getProductionRate());
            entity.progress += consumeAmount;
        }

        // Determine what crystal we're making and its cost
        ItemStack outputItems = entity.getItem(SLOT_RESULT);
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

        boolean changed = entity.checkConsumeFuelItem();

        int itemCost = CRYSTAL_COSTS.get(producingItem).get();
        Item nextItem = CRYSTAL_STEPS.get(producingItem);
        entity.goal = itemCost;

        // Produce the item if we can, replacing the existing item
        if (entity.progress >= itemCost) {
            entity.progress -= itemCost;
            changed = true;
            if (outputItems.isEmpty()) {
                entity.setItem(SLOT_RESULT, producingItem.getDefaultInstance());
            } else if (outputItems.getCount() == GROW_CRYSTAL_AT - 1 && nextItem != null) {
                entity.setItem(SLOT_RESULT, nextItem.getDefaultInstance());
                entity.goal = CRYSTAL_COSTS.get(nextItem).get();
            } else {
                outputItems.grow(1);
            }
        }

        if (changed) {
            setChanged(level, blockPos, blockState);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        return SLOTS_FOR_DOWN;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return slot == SLOT_RESULT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        if (slot == SLOT_RESULT) {
            return false;
        }
        return super.canPlaceItem(slot, item);
    }

}
