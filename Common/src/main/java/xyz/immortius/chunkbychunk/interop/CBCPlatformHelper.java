package xyz.immortius.chunkbychunk.interop;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import xyz.immortius.chunkbychunk.common.blockEntities.*;
import xyz.immortius.chunkbychunk.common.menus.BedrockChestMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldForgeMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldMenderMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;

import java.util.List;

public interface CBCPlatformHelper {
    /// Blocks

    Block spawnChunkBlock();

    Block unstableSpawnChunkBlock();

    Block bedrockChestBlock();

    Block worldCoreBlock();

    Block worldForgeBlock();

    Block worldScannerBlock();

    // Block Items

    Item spawnChunkBlockItem();

    Item unstableChunkSpawnBlockItem();

    Item bedrockChestItem();

    Item worldCoreBlockItem();

    Item worldForgeBlockItem();

    Item worldScannerBlockItem();

    Item worldMenderBlockItem();

    // Items

    Item worldFragmentItem();

    Item worldShardItem();

    Item worldCrystalItem();

    List<ItemStack> biomeThemeBlockItems();

    // Block Entities

    BlockEntityType<BedrockChestBlockEntity> bedrockChestEntity();

    BlockEntityType<WorldForgeBlockEntity> worldForgeEntity();

    BlockEntityType<WorldScannerBlockEntity> worldScannerEntity();

    BlockEntityType<WorldMenderBlockEntity> worldMenderEntity();

    // Sound Events

    SoundEvent spawnChunkSoundEffect();

    // Menus

    MenuType<BedrockChestMenu> bedrockChestMenu();

    MenuType<WorldForgeMenu> worldForgeMenu();

    MenuType<WorldScannerMenu> worldScannerMenu();

    MenuType<WorldMenderMenu> worldMenderMenu();

    // Fluid Access

    Fluid getFluidContent(BucketItem bucketItem);

}
