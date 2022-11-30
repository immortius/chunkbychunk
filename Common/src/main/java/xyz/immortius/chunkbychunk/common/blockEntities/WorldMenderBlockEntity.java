package xyz.immortius.chunkbychunk.common.blockEntities;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import xyz.immortius.chunkbychunk.common.blocks.BaseSpawnChunkBlock;
import xyz.immortius.chunkbychunk.common.data.ScannerData;
import xyz.immortius.chunkbychunk.common.menus.WorldMenderMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;
import xyz.immortius.chunkbychunk.common.util.ChunkUtil;
import xyz.immortius.chunkbychunk.common.util.SpiralIterator;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.*;

/**
 * World Scanner Block Entity - this consumes crystals in order to scan for a provided block or item hint.
 */
public class WorldMenderBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, StackedContentsCompatible {

    public static final int NUM_ITEM_SLOTS = 1;
    public static final int NUM_DATA_ITEMS = 0;
    public static final int SLOT_INPUT = 0;

    public static final int TICKS_BETWEEN_GENERATION = 120;

    private NonNullList<ItemStack> items;
    private int cooldown;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int id) {
            return switch (id) {
                default -> 0;
            };
        }

        public void set(int id, int value) {
            switch (id) {
            }
        }

        public int getCount() {
            return NUM_DATA_ITEMS;
        }
    };

    public WorldMenderBlockEntity(BlockPos pos, BlockState state) {
        super(Services.PLATFORM.worldMenderEntity(), pos, state);
        items = NonNullList.withSize(NUM_ITEM_SLOTS, ItemStack.EMPTY);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.chunkbychunk.worldmender");
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, WorldMenderBlockEntity entity) {
        if (entity.cooldown > 0) {
            entity.cooldown--;
            return;
        }
        if (entity.validInput()) {
            ChunkPos centerPos = new ChunkPos(blockPos);
            SpiralIterator spiralIterator = new SpiralIterator(centerPos.x, centerPos.z);
            while (spiralIterator.layerDistance() <= 8) {
                ChunkPos chunkPos = new ChunkPos(spiralIterator.getX(), spiralIterator.getY());
                if (SpawnChunkHelper.isEmptyChunk(level, chunkPos)) {
                    BlockPos pos = chunkPos.getMiddleBlockPosition(level.getMaxBuildHeight() - 1);
                    level.setBlock(pos, Services.PLATFORM.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_NONE);
                    entity.getItem(SLOT_INPUT).shrink(1);
                    entity.cooldown = TICKS_BETWEEN_GENERATION;

                    spiralIterator.next();
                    if (spiralIterator.layerDistance() <= 8) {
                        //level.getServer().getLevel(((SkyChunkGenerator) ((ServerLevel)level).getChunkSource().getGenerator()).getGenerationLevel()).setChunkForced(spiralIterator.getX(), spiralIterator.getY(), true);
                    }
                    break;
                }
                spiralIterator.next();
            }

        }
    }

    private boolean validInput() {
        ItemStack targetItem = getItem(SLOT_INPUT);
        return targetItem.getItem() instanceof BlockItem bi && (bi.getBlock().equals(Services.PLATFORM.worldCoreBlock()) || bi.getBlock() instanceof BaseSpawnChunkBlock);
    }

    // Container methods

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
    public void setItem(int slot, ItemStack newItem) {
        items.set(slot, newItem);
        if (newItem.getCount() > this.getMaxStackSize()) {
            newItem.setCount(this.getMaxStackSize());
        }
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
    public void clearContent() {
        items.clear();
    }

    // Worldy Container

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        if (Services.PLATFORM.worldCoreBlockItem().equals(item.getItem())) {
            return true;
        } else if (item.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof BaseSpawnChunkBlock;
        } else {
            return false;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return true;
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (ItemStack itemstack : items) {
            contents.accountStack(itemstack);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch (direction) {
            default -> new int[] {SLOT_INPUT};
        };
    }

    // UI access

    @Override
    protected AbstractContainerMenu createMenu(int menuId, Inventory inventory) {
        return new WorldMenderMenu(menuId, inventory, this, this.dataAccess);
    }

    @Override
    public boolean stillValid(Player player) {
        if (level.getBlockEntity(worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    // Serialization

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.cooldown = tag.getInt("Cooldown");
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("Cooldown", cooldown);
    }

}
