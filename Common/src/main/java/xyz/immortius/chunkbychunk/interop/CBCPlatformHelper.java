package xyz.immortius.chunkbychunk.interop;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import xyz.immortius.chunkbychunk.common.blockEntities.*;
import xyz.immortius.chunkbychunk.common.menus.BedrockChestMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldForgeMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;

public interface CBCPlatformHelper {
    /// Blocks

    Block spawnChunkBlock();

    Block unstableSpawnChunkBlock();

    Block bedrockChestBlock();

    Block worldCoreBlock();

    Block worldForgeBlock();

    Block worldScannerBlock();

    Block triggeredSpawnChunkBlock();

    Block triggeredSpawnRandomChunkBlock();

    // Block Items

    Item spawnChunkBlockItem();

    Item unstableChunkSpawnBlockItem();

    Item bedrockChestItem();

    Item worldCoreBlockItem();

    Item worldForgeBlockItem();

    Item worldScannerBlockItem();

    // Items

    Item worldFragmentItem();

    Item worldShardItem();

    Item worldCrystalItem();

    // Block Entities

    BlockEntityType<BedrockChestBlockEntity> bedrockChestEntity();

    BlockEntityType<WorldForgeBlockEntity> worldForgeEntity();

    BlockEntityType<WorldScannerBlockEntity> worldScannerEntity();

    BlockEntityType<TriggeredSpawnChunkBlockEntity> triggeredSpawnChunkEntity();

    BlockEntityType<TriggeredSpawnRandomChunkBlockEntity> triggeredSpawnRandomChunkEntity();

    // Sound Events

    SoundEvent spawnChunkSoundEffect();

    // Menus

    MenuType<BedrockChestMenu> bedrockChestMenu();

    MenuType<WorldForgeMenu> worldForgeMenu();

    MenuType<WorldScannerMenu> worldScannerMenu();
}
