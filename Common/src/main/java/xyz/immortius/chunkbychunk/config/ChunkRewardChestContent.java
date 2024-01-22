package xyz.immortius.chunkbychunk.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import xyz.immortius.chunkbychunk.common.util.ChunkUtil;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Enumeration for types of contents allowed in the chunk reward chest
 */
public enum ChunkRewardChestContent {
    ChunkSpawner(Services.PLATFORM::spawnChunkBlockItem),
    UnstableChunkSpawner(Services.PLATFORM::unstableChunkSpawnBlockItem),
    WorldCore(Services.PLATFORM::worldCoreBlockItem),
    WorldCrystal(Services.PLATFORM::worldCrystalItem),
    WorldForge(Services.PLATFORM::worldForgeBlockItem),
    WorldFragment(Services.PLATFORM::worldFragmentItem),
    Random(null) {

        private static final List<RNGSlot> mainSlot = ImmutableList.of(
                new RNGSlot(Services.PLATFORM::spawnChunkBlockItem, 1, 1, 30),
                new RNGSlot(Services.PLATFORM::unstableChunkSpawnBlockItem, 1, 1, 60),
                new RNGSlot(Services.PLATFORM::worldCoreBlockItem, 1, 1, 10)
        );
        private static final int mainSlotRange = mainSlot.stream().mapToInt(x -> x.chance).sum();

        private static final List<RNGSlot> additionalSlot = ImmutableList.of(
                new RNGSlot(Services.PLATFORM::worldFragmentItem, 1, 16, 60),
                new RNGSlot(Services.PLATFORM::worldShardItem, 1, 8, 30),
                new RNGSlot(Services.PLATFORM::worldCrystalItem, 1, 2, 15),
                new RNGSlot(Services.PLATFORM::worldCoreBlockItem, 1, 1, 5),
                new RNGSlot(Services.PLATFORM::worldForgeBlockItem, 1, 1, 2),
                new RNGSlot(Services.PLATFORM::worldScannerBlockItem, 1, 1, 1),
                new RNGSlot(Services.PLATFORM::worldMenderBlockItem, 1, 1, 1)
        );
        private static final int additionalSlotRange = additionalSlot.stream().mapToInt(x -> x.chance).sum();

        private static final float additionalChance = 0.25f;

        @Override
        public List<ItemStack> getItems(Random random, int quantity) {
            List<ItemStack> result = new ArrayList<>();
            int roll = random.nextInt(mainSlotRange);
            int mainSlotChance = 0;
            for (RNGSlot rngSlot : mainSlot) {
                mainSlotChance += rngSlot.chance;
                if (roll < mainSlotChance) {
                    ItemStack stack = rngSlot.item.get().getDefaultInstance();
                    stack.setCount(random.nextInt(rngSlot.minQuanity, rngSlot.maxQuantity + 1));
                    result.add(stack);
                    break;
                }
            }
            do {
                roll = random.nextInt(additionalSlotRange);
                int addSlotChance = 0;
                for (RNGSlot rngSlot : additionalSlot) {
                    addSlotChance += rngSlot.chance;
                    if (roll < addSlotChance) {
                        ItemStack stack = rngSlot.item.get().getDefaultInstance();
                        stack.setCount(random.nextInt(rngSlot.minQuanity, rngSlot.maxQuantity + 1));
                        result.add(stack);
                        break;
                    }
                }
            } while (result.size() < 8 && random.nextFloat() < additionalChance);
            return result;
        }
    };

    private final Supplier<Item> item;

    ChunkRewardChestContent(Supplier<Item> item) {
        this.item = item;
    }

    public List<ItemStack> getItems(Random random, int quantity) {
        ItemStack result = item.get().getDefaultInstance();
        result.setCount(quantity);
        return Collections.singletonList(result);
    }

    private record RNGSlot(Supplier<Item> item, int minQuanity, int maxQuantity, int chance) {}

}
