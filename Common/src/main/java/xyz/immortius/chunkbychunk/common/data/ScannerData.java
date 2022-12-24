package xyz.immortius.chunkbychunk.common.data;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldScannerBlockEntity;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scanner Data provides a mapping between input items for the world scanner and what blocks they scan for.
 */
public class ScannerData {

    private final Set<String> inputItems = new LinkedHashSet<>();
    private final Set<String> targetBlocks = new LinkedHashSet<>();

    public ScannerData(Collection<String> items, Collection<String> blocks) {
        this.inputItems.addAll(items);
        this.targetBlocks.addAll(blocks);
    }

    public void process(ResourceLocation context) {
        Set<Item> inputItems = getInputItems(context);
        Set<Block> targetBlocks = getTargetBlocks(context);
        if (!inputItems.isEmpty() && !targetBlocks.isEmpty()) {
            WorldScannerBlockEntity.addItemMappings(inputItems, targetBlocks);
        } else {
            ChunkByChunkConstants.LOGGER.error("Invalid scanner data '{}', missing source items or target blocks", context);
        }
    }

    private Set<Block> getTargetBlocks(ResourceLocation context) {
        return targetBlocks.stream()
                .map(x -> {
                    Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(x));
                    if (block.isEmpty()) {
                        ChunkByChunkConstants.LOGGER.warn("Could not resolve block {} in scanner data {}", x, context);
                    }
                    return block;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Set<Item> getInputItems(ResourceLocation context) {
        return inputItems.stream()
                .map(x -> {
                    Optional<Item> item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(x));
                    if (item.isEmpty()) {
                        ChunkByChunkConstants.LOGGER.warn("Could not resolve item {} in scanner data {}", x, context);
                    }
                    return item;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}
