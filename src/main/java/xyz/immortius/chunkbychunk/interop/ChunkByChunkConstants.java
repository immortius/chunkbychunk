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
import xyz.immortius.chunkbychunk.common.menus.BedrockChestMenu;
import xyz.immortius.chunkbychunk.forge.ChunkByChunkMod;

/**
 * Constants for ChunkByChunk - may vary by mod system
 */
public final class ChunkByChunkConstants {

    private ChunkByChunkConstants() {
    }

    public static final String MOD_ID = "chunkbychunk";

    public static final ResourceKey<Level> SKY_CHUNK_GENERATION_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "skychunkgeneration"));

    public static Block spawnChunkBlock() {
        return ChunkByChunkMod.SPAWN_CHUNK_BLOCK.get();
    }

    public static Block unstableSpawnChunkBlock() {
        return ChunkByChunkMod.UNSTABLE_SPAWN_CHUNK_BLOCK.get();
    }

    public static Block bedrockChestBlock() {
        return ChunkByChunkMod.BEDROCK_CHEST_BLOCK.get();
    }

    public static Item spawnChunkBlockItem() {
        return ChunkByChunkMod.SPAWN_CHUNK_BLOCK_ITEM.get();
    }

    public static Item unstableChunkSpawnBlockItem() {
        return ChunkByChunkMod.UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM.get();
    }

    public static Item bedrockChestItem() {
        return ChunkByChunkMod.BEDROCK_CHEST_ITEM.get();
    }

    public static BlockEntityType<BedrockChestBlockEntity> bedrockChestEntity() {
        return (BlockEntityType<BedrockChestBlockEntity>) ChunkByChunkMod.BEDROCK_CHEST_BLOCK_ENTITY.get();
    }

    public static SoundEvent spawnChunkSoundEffect() {
        return ChunkByChunkMod.SPAWN_CHUNK_SOUND_EVENT.get();
    }

    public static MenuType<BedrockChestMenu> bedrockChestMenu() {
        return (MenuType<BedrockChestMenu>) ChunkByChunkMod.BEDROCK_CHEST_MENU.get();
    }
}
