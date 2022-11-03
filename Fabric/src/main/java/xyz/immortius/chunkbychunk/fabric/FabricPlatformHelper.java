package xyz.immortius.chunkbychunk.fabric;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import xyz.immortius.chunkbychunk.common.blockEntities.*;
import xyz.immortius.chunkbychunk.common.menus.BedrockChestMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldForgeMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;
import xyz.immortius.chunkbychunk.interop.CBCPlatformHelper;
import xyz.immortius.chunkbychunk.mixins.BucketFluidAccessor;

/**
 * Static methods whose implementation varies by mod system
 */
public final class FabricPlatformHelper implements CBCPlatformHelper {
    /// Blocks

    public Block spawnChunkBlock() {
        return ChunkByChunkMod.SPAWN_CHUNK_BLOCK;
    }

    public Block unstableSpawnChunkBlock() {
        return ChunkByChunkMod.UNSTABLE_SPAWN_CHUNK_BLOCK;
    }

    public Block bedrockChestBlock() {
        return ChunkByChunkMod.BEDROCK_CHEST_BLOCK;
    }

    public Block worldCoreBlock() { return ChunkByChunkMod.WORLD_CORE_BLOCK; }

    public Block worldForgeBlock() { return ChunkByChunkMod.WORLD_FORGE_BLOCK; }

    public Block worldScannerBlock() { return ChunkByChunkMod.WORLD_SCANNER_BLOCK; }

    public Block triggeredSpawnChunkBlock() { return ChunkByChunkMod.TRIGGERED_SPAWN_CHUNK_BLOCK; }

    public Block triggeredSpawnRandomChunkBlock() { return ChunkByChunkMod.TRIGGERED_SPAWN_RANDOM_CHUNK_BLOCK; }

    // Block Items

    public Item spawnChunkBlockItem() {
        return ChunkByChunkMod.SPAWN_CHUNK_BLOCK_ITEM;
    }

    public Item unstableChunkSpawnBlockItem() {
        return ChunkByChunkMod.UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM;
    }

    public Item bedrockChestItem() {
        return ChunkByChunkMod.BEDROCK_CHEST_BLOCK_ITEM;
    }

    public Item worldCoreBlockItem() { return ChunkByChunkMod.WORLD_CORE_BLOCK_ITEM; }

    public Item worldForgeBlockItem() { return ChunkByChunkMod.WORLD_FORGE_BLOCK_ITEM; }

    public Item worldScannerBlockItem() { return ChunkByChunkMod.WORLD_SCANNER_BLOCK_ITEM; }

    // Items

    public Item worldFragmentItem() { return ChunkByChunkMod.WORLD_FRAGMENT_ITEM; }

    public Item worldShardItem() { return ChunkByChunkMod.WORLD_SHARD_ITEM; }

    public Item worldCrystalItem() { return ChunkByChunkMod.WORLD_CRYSTAL_ITEM; }

    // Block Entities

    public BlockEntityType<BedrockChestBlockEntity> bedrockChestEntity() {
        return (BlockEntityType<BedrockChestBlockEntity>) ChunkByChunkMod.BEDROCK_CHEST_BLOCK_ENTITY;
    }

    public BlockEntityType<WorldForgeBlockEntity> worldForgeEntity() {
        return (BlockEntityType<WorldForgeBlockEntity>) ChunkByChunkMod.WORLD_FORGE_BLOCK_ENTITY;
    }

    public BlockEntityType<WorldScannerBlockEntity> worldScannerEntity() {
        return (BlockEntityType<WorldScannerBlockEntity>) ChunkByChunkMod.WORLD_SCANNER_BLOCK_ENTITY;
    }

    public BlockEntityType<TriggeredSpawnChunkBlockEntity> triggeredSpawnChunkEntity() {
        return (BlockEntityType<TriggeredSpawnChunkBlockEntity>) ChunkByChunkMod.TRIGGERED_SPAWN_CHUNK_BLOCK_ENTITY;
    }

    public BlockEntityType<TriggeredSpawnRandomChunkBlockEntity> triggeredSpawnRandomChunkEntity() {
        return (BlockEntityType<TriggeredSpawnRandomChunkBlockEntity>) ChunkByChunkMod.TRIGGERED_SPAWN_RANDOM_CHUNK_BLOCK_ENTITY;
    }

    // Sound Events

    public SoundEvent spawnChunkSoundEffect() {
        return ChunkByChunkMod.SPAWN_CHUNK_SOUND_EVENT;
    }

    // Menus

    public MenuType<BedrockChestMenu> bedrockChestMenu() {
        return ChunkByChunkMod.BEDROCK_CHEST_MENU;
    }

    public MenuType<WorldForgeMenu> worldForgeMenu() {
        return ChunkByChunkMod.WORLD_FORGE_MENU;
    }

    public MenuType<WorldScannerMenu> worldScannerMenu() {
        return ChunkByChunkMod.WORLD_SCANNER_MENU;
    }

    @Override
    public Fluid getFluidContent(BucketItem bucketItem) {
        return ((BucketFluidAccessor) bucketItem).getFluidContent();
    }

}
