package xyz.immortius.chunkbychunk.forge;

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
import xyz.immortius.chunkbychunk.interop.CBCPlatformHelper;

import java.util.List;
import java.util.function.Supplier;

/**
 * Static methods whose implementation varies by mod system
 */
public final class ForgePlatformHelper implements CBCPlatformHelper {
    /// Blocks

    @Override
    public Block spawnChunkBlock() {
        return ChunkByChunkMod.SPAWN_CHUNK_BLOCK.get();
    }

    @Override
    public Block unstableSpawnChunkBlock() {
        return ChunkByChunkMod.UNSTABLE_SPAWN_CHUNK_BLOCK.get();
    }

    @Override
    public Block bedrockChestBlock() {
        return ChunkByChunkMod.BEDROCK_CHEST_BLOCK.get();
    }

    @Override
    public Block worldCoreBlock() { return ChunkByChunkMod.WORLD_CORE_BLOCK.get(); }

    @Override
    public Block worldForgeBlock() { return ChunkByChunkMod.WORLD_FORGE_BLOCK.get(); }

    @Override
    public Block worldScannerBlock() { return ChunkByChunkMod.WORLD_SCANNER_BLOCK.get(); }

    @Override
    public Block triggeredSpawnChunkBlock() { return ChunkByChunkMod.TRIGGERED_SPAWN_CHUNK_BLOCK.get(); }

    @Override
    public Block triggeredSpawnRandomChunkBlock() { return ChunkByChunkMod.TRIGGERED_SPAWN_RANDOM_CHUNK_BLOCK.get(); }

    // Block Items
    @Override
    public Item spawnChunkBlockItem() {
        return ChunkByChunkMod.SPAWN_CHUNK_BLOCK_ITEM.get();
    }

    @Override
    public Item unstableChunkSpawnBlockItem() {
        return ChunkByChunkMod.UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM.get();
    }

    @Override
    public Item bedrockChestItem() {
        return ChunkByChunkMod.BEDROCK_CHEST_ITEM.get();
    }

    @Override
    public Item worldCoreBlockItem() { return ChunkByChunkMod.WORLD_CORE_BLOCK_ITEM.get(); }

    @Override
    public Item worldForgeBlockItem() { return ChunkByChunkMod.WORLD_FORGE_BLOCK_ITEM.get(); }

    @Override
    public Item worldScannerBlockItem() { return ChunkByChunkMod.WORLD_SCANNER_BLOCK_ITEM.get(); }

    @Override
    public Item worldMenderBlockItem() { return ChunkByChunkMod.WORLD_MENDER_BLOCK_ITEM.get(); }

    // Items
    @Override
    public Item worldFragmentItem() { return ChunkByChunkMod.WORLD_FRAGMENT_ITEM.get(); }

    @Override
    public Item worldShardItem() { return ChunkByChunkMod.WORLD_SHARD_ITEM.get(); }

    @Override
    public Item worldCrystalItem() { return ChunkByChunkMod.WORLD_CRYSTAL_ITEM.get(); }

    @Override
    public List<ItemStack> biomeThemeBlockItems() {
        return ChunkByChunkMod.THEMED_SPAWN_CHUNK_ITEMS.stream().map(Supplier::get).toList();
    }

    // Block Entities
    @Override
    public BlockEntityType<BedrockChestBlockEntity> bedrockChestEntity() {
        return (BlockEntityType<BedrockChestBlockEntity>) ChunkByChunkMod.BEDROCK_CHEST_BLOCK_ENTITY.get();
    }

    @Override
    public BlockEntityType<WorldForgeBlockEntity> worldForgeEntity() {
        return (BlockEntityType<WorldForgeBlockEntity>) ChunkByChunkMod.WORLD_FORGE_BLOCK_ENTITY.get();
    }

    @Override
    public BlockEntityType<WorldScannerBlockEntity> worldScannerEntity() {
        return (BlockEntityType<WorldScannerBlockEntity>) ChunkByChunkMod.WORLD_SCANNER_BLOCK_ENTITY.get();
    }

    @Override
    public BlockEntityType<WorldMenderBlockEntity> worldMenderEntity() {
        return (BlockEntityType<WorldMenderBlockEntity>) ChunkByChunkMod.WORLD_MENDER_BLOCK_ENTITY.get();
    }

    @Override
    public BlockEntityType<TriggeredSpawnChunkBlockEntity> triggeredSpawnChunkEntity() {
        return (BlockEntityType<TriggeredSpawnChunkBlockEntity>) ChunkByChunkMod.TRIGGERED_SPAWN_CHUNK_BLOCK_ENTITY.get();
    }

    @Override
    public BlockEntityType<TriggeredSpawnRandomChunkBlockEntity> triggeredSpawnRandomChunkEntity() {
        return (BlockEntityType<TriggeredSpawnRandomChunkBlockEntity>) ChunkByChunkMod.TRIGGERED_SPAWN_RANDOM_CHUNK_BLOCK_ENTITY.get();
    }

    // Sound Events
    @Override
    public SoundEvent spawnChunkSoundEffect() {
        return ChunkByChunkMod.SPAWN_CHUNK_SOUND_EVENT.get();
    }

    // Menus
    @Override
    public MenuType<BedrockChestMenu> bedrockChestMenu() {
        return ChunkByChunkMod.BEDROCK_CHEST_MENU.get();
    }

    @Override
    public MenuType<WorldForgeMenu> worldForgeMenu() {
        return ChunkByChunkMod.WORLD_FORGE_MENU.get();
    }

    @Override
    public MenuType<WorldScannerMenu> worldScannerMenu() {
        return ChunkByChunkMod.WORLD_SCANNER_MENU.get();
    }

    @Override
    public MenuType<WorldMenderMenu> worldMenderMenu() {
        return ChunkByChunkMod.WORLD_MENDER_MENU.get();
    }

    @Override
    public Fluid getFluidContent(BucketItem bucketItem) {
        return bucketItem.getFluid();
    }
}
