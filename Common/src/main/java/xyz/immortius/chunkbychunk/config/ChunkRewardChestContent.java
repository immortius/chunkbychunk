package xyz.immortius.chunkbychunk.config;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.function.Supplier;

/**
 * Enumeration for types of contents allowed in the chunk reward chest
 */
public enum ChunkRewardChestContent {
    ChunkSpawner(Services.PLATFORM::spawnChunkBlockItem),
    UnstableChunkSpawner(Services.PLATFORM::unstableChunkSpawnBlockItem),
    WorldCore(Services.PLATFORM::worldCoreBlockItem),
    WorldCrystal(Services.PLATFORM::worldCrystalItem);

    private final Supplier<Item> item;

    ChunkRewardChestContent(Supplier<Item> item) {
        this.item = item;
    }

    public ItemStack getItem(int quantity) {
        ItemStack result = item.get().getDefaultInstance();
        result.setCount(quantity);
        return result;
    }
}
