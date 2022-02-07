package xyz.immortius.chunkbychunk.interop;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import xyz.immortius.chunkbychunk.common.blockEntities.BedrockChestBlockEntity;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldForgeBlockEntity;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldScannerBlockEntity;
import xyz.immortius.chunkbychunk.common.menus.BedrockChestMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldForgeMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;
import xyz.immortius.chunkbychunk.forge.ChunkByChunkMod;

/**
 * Constants for ChunkByChunk - may vary by mod system
 */
public final class ChunkByChunkConstants {

    private ChunkByChunkConstants() {
    }

    public static final String MOD_ID = "chunkbychunk";

    public static final ResourceKey<Level> SKY_CHUNK_GENERATION_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "skychunkgeneration"));

    /// Blocks

    public static Block spawnChunkBlock() {
        return ChunkByChunkMod.SPAWN_CHUNK_BLOCK.get();
    }

    public static Block unstableSpawnChunkBlock() {
        return ChunkByChunkMod.UNSTABLE_SPAWN_CHUNK_BLOCK.get();
    }

    public static Block bedrockChestBlock() {
        return ChunkByChunkMod.BEDROCK_CHEST_BLOCK.get();
    }

    public static Block worldCoreBlock() { return ChunkByChunkMod.WORLD_CORE_BLOCK.get(); }

    public static Block worldForgeBlock() { return ChunkByChunkMod.WORLD_FORGE_BLOCK.get(); }

    public static Block worldScannerBlock() { return ChunkByChunkMod.WORLD_SCANNER_BLOCK.get(); }

    // Block Items

    public static Item spawnChunkBlockItem() {
        return ChunkByChunkMod.SPAWN_CHUNK_BLOCK_ITEM.get();
    }

    public static Item unstableChunkSpawnBlockItem() {
        return ChunkByChunkMod.UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM.get();
    }

    public static Item bedrockChestItem() {
        return ChunkByChunkMod.BEDROCK_CHEST_ITEM.get();
    }

    public static Item worldCoreBlockItem() { return ChunkByChunkMod.WORLD_CORE_BLOCK_ITEM.get(); }

    public static Item worldForgeBlockItem() { return ChunkByChunkMod.WORLD_FORGE_BLOCK_ITEM.get(); }

    public static Item worldScannerBlockItem() { return ChunkByChunkMod.WORLD_SCANNER_BLOCK_ITEM.get(); }

    // Items

    public static Item worldFragmentItem() { return ChunkByChunkMod.WORLD_FRAGMENT_ITEM.get(); }

    public static Item worldShardItem() { return ChunkByChunkMod.WORLD_SHARD_ITEM.get(); }

    public static Item worldCrystalItem() { return ChunkByChunkMod.WORLD_CRYSTAL_ITEM.get(); }

    // Block Entities

    public static BlockEntityType<BedrockChestBlockEntity> bedrockChestEntity() {
        return (BlockEntityType<BedrockChestBlockEntity>) ChunkByChunkMod.BEDROCK_CHEST_BLOCK_ENTITY.get();
    }

    public static BlockEntityType<WorldForgeBlockEntity> worldForgeEntity() {
        return (BlockEntityType<WorldForgeBlockEntity>) ChunkByChunkMod.WORLD_FORGE_BLOCK_ENTITY.get();
    }

    public static BlockEntityType<WorldScannerBlockEntity> worldScannerEntity() {
        return (BlockEntityType<WorldScannerBlockEntity>) ChunkByChunkMod.WORLD_SCANNER_BLOCK_ENTITY.get();
    }

    // Sound Events

    public static SoundEvent spawnChunkSoundEffect() {
        return ChunkByChunkMod.SPAWN_CHUNK_SOUND_EVENT.get();
    }

    // Menus

    public static MenuType<BedrockChestMenu> bedrockChestMenu() {
        return ChunkByChunkMod.BEDROCK_CHEST_MENU.get();
    }

    public static MenuType<WorldForgeMenu> worldForgeMenu() {
        return ChunkByChunkMod.WORLD_FORGE_MENU.get();
    }

    public static MenuType<WorldScannerMenu> worldScannerMenu() {
        return ChunkByChunkMod.WORLD_SCANNER_MENU.get();
    }
}
