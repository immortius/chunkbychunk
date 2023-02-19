package xyz.immortius.chunkbychunk.common.menus;

import com.google.common.base.Preconditions;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldMenderBlockEntity;
import xyz.immortius.chunkbychunk.common.blocks.BaseSpawnChunkBlock;
import xyz.immortius.chunkbychunk.interop.Services;

/**
 * Menu for interacting with the world mender
 */
public class WorldMenderMenu extends BaseInventoryContainerMenu {
    private final ContainerData containerData;

    public WorldMenderMenu(int menuId, Inventory inventory) {
        this(menuId, inventory, new SimpleContainer(WorldMenderBlockEntity.NUM_ITEM_SLOTS), new SimpleContainerData(WorldMenderBlockEntity.NUM_DATA_ITEMS));
    }

    public WorldMenderMenu(int menuId, Inventory inventory, Container container, ContainerData containerData) {
        super(Services.PLATFORM.worldMenderMenu(), menuId, container, inventory, 8, 153);
        this.containerData = containerData;
        Preconditions.checkArgument(container.getContainerSize() >= WorldMenderBlockEntity.NUM_ITEM_SLOTS, "Expected " + WorldMenderBlockEntity.NUM_ITEM_SLOTS + " item slots, but entity has " + container.getContainerSize());
        Preconditions.checkArgument(containerData.getCount() >= WorldMenderBlockEntity.NUM_DATA_ITEMS, "Expected " + WorldMenderBlockEntity.NUM_DATA_ITEMS + " data items, but entity has " + containerData.getCount());

        addSlot(new FilteredSlot(container, WorldMenderBlockEntity.SLOT_INPUT, 80, 69, x -> x.getItem() instanceof BlockItem bi && (bi.getBlock().equals(Services.PLATFORM.worldCoreBlock()) || bi.getBlock() instanceof BaseSpawnChunkBlock)));

        addDataSlots(containerData);
    }

    @Override
    protected boolean quickMoveToContainer(ItemStack stack) {
        return this.moveItemStackToContainerSlot(stack, WorldMenderBlockEntity.SLOT_INPUT, WorldMenderBlockEntity.SLOT_INPUT + 1, false);
    }

    public int getChunksSpawned() {
        return containerData.get(WorldMenderBlockEntity.DATA_CHUNKS_SPAWNED);
    }
}
