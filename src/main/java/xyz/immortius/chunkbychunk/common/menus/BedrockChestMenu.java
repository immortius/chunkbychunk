package xyz.immortius.chunkbychunk.common.menus;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import xyz.immortius.chunkbychunk.common.blockEntities.BedrockChestBlockEntity;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

/**
 * Menu for the bedrock chest. It has a single item slot.
 */
public class BedrockChestMenu extends BaseInventoryContainerMenu {

    public BedrockChestMenu(int menuId, Inventory inventory) {
        this(menuId, inventory, new SimpleContainer(BedrockChestBlockEntity.CONTAINER_SIZE));
    }

    public BedrockChestMenu(int menuId, Inventory inventory, Container container) {
        super(ChunkByChunkConstants.bedrockChestMenu(), menuId, container, inventory, 8, 84);
        checkContainerSize(container, BedrockChestBlockEntity.CONTAINER_SIZE);

        for(int k = 0; k < BedrockChestBlockEntity.ROWS; ++k) {
            for(int l = 0; l < BedrockChestBlockEntity.COLUMNS; ++l) {
                this.addSlot(new Slot(container, 0, 8 + 4 * INVENTORY_SLOT_PIXELS, 18 + INVENTORY_SLOT_PIXELS));
            }
        }
    }

}
