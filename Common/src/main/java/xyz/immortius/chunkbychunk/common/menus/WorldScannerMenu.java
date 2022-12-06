package xyz.immortius.chunkbychunk.common.menus;

import com.google.common.base.Preconditions;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldScannerBlockEntity;
import xyz.immortius.chunkbychunk.interop.Services;

/**
 * Menu for interacting with the world scanner
 */
public class WorldScannerMenu extends BaseInventoryContainerMenu {
    private final ContainerData containerData;

    public WorldScannerMenu(int menuId, Inventory inventory) {
        this(menuId, inventory, new SimpleContainer(WorldScannerBlockEntity.NUM_ITEM_SLOTS), new SimpleContainerData(WorldScannerBlockEntity.NUM_DATA_ITEMS));
    }

    public WorldScannerMenu(int menuId, Inventory inventory, Container container, ContainerData containerData) {
        super(Services.PLATFORM.worldScannerMenu(), menuId, container, inventory, 8, 84);
        this.containerData = containerData;
        Preconditions.checkArgument(container.getContainerSize() >= WorldScannerBlockEntity.NUM_ITEM_SLOTS, "Expected " + WorldScannerBlockEntity.NUM_ITEM_SLOTS + " item slots, but entity has " + container.getContainerSize());
        Preconditions.checkArgument(containerData.getCount() >= WorldScannerBlockEntity.NUM_DATA_ITEMS, "Expected " + WorldScannerBlockEntity.NUM_DATA_ITEMS + " data items, but entity has " + containerData.getCount());

        addSlot(new Slot(container, WorldScannerBlockEntity.SLOT_INPUT, 27, 21));
        addSlot(new FilteredSlot(container, WorldScannerBlockEntity.SLOT_FUEL, 27, 50, WorldScannerBlockEntity::isWorldScannerFuel));

        addDataSlots(containerData);
    }

    public int getEnergy() {
        return this.containerData.get(WorldScannerBlockEntity.DATA_ENERGY);
    }

    public int getMaxEnergy() {
        return this.containerData.get(WorldScannerBlockEntity.DATA_MAX_ENERGY);
    }

    public int getCurrentChunkX() {
        return this.containerData.get(WorldScannerBlockEntity.DATA_SCANNING_X);
    }

    public int getCurrentChunkZ() {
        return this.containerData.get(WorldScannerBlockEntity.DATA_SCANNING_Z);
    }

    public boolean isMapAvailable() {
        return this.containerData.get(WorldScannerBlockEntity.DATA_MAP) != WorldScannerBlockEntity.NO_MAP;
    }

    public String getMapKey() {
        return MapItem.makeKey(this.containerData.get(WorldScannerBlockEntity.DATA_MAP));
    }

    public int getMapId() {
        return this.containerData.get(WorldScannerBlockEntity.DATA_MAP);
    }

    @Override
    protected boolean quickMoveToContainer(ItemStack stack) {
        if (WorldScannerBlockEntity.isWorldScannerFuel(stack)) {
            return this.moveItemStackToContainerSlot(stack, WorldScannerBlockEntity.SLOT_FUEL, WorldScannerBlockEntity.SLOT_FUEL + 1, false);
        }
        return this.moveItemStackToContainerSlot(stack, WorldScannerBlockEntity.SLOT_INPUT, WorldScannerBlockEntity.SLOT_INPUT + 1, false);
    }

}
