package xyz.immortius.chunkbychunk.config;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.util.function.Supplier;

public enum BedrockChestContents {
    ChunkSpawner(ChunkByChunkConstants::spawnChunkBlockItem),
    UnstableChunkSpawner(ChunkByChunkConstants::unstableChunkSpawnBlockItem),
    WorldCore(ChunkByChunkConstants::worldCoreBlockItem),
    WorldCrystal(ChunkByChunkConstants::worldCrystalItem);

    private final Supplier<Item> item;

    BedrockChestContents(Supplier<Item> item) {
        this.item = item;
    }

    public ItemStack getItem(int quantity) {
        ItemStack result = item.get().getDefaultInstance();
        result.setCount(quantity);
        return result;
    }
}
