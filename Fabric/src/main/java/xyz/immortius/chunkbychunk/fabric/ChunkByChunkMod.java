package xyz.immortius.chunkbychunk.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.common.CommonEventHandler;
import xyz.immortius.chunkbychunk.common.blockEntities.*;
import xyz.immortius.chunkbychunk.common.blocks.*;
import xyz.immortius.chunkbychunk.common.commands.SpawnChunkCommand;
import xyz.immortius.chunkbychunk.common.menus.BedrockChestMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldForgeMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.config.system.ConfigSystem;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.server.ServerEventHandler;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Common mod initialization
 */
public class ChunkByChunkMod implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);

    public static final Block TRIGGERED_SPAWN_CHUNK_BLOCK = new TriggeredSpawnChunkBlock(FabricBlockSettings.of(Material.AIR));
    public static final Block TRIGGERED_SPAWN_RANDOM_CHUNK_BLOCK = new TriggeredSpawnRandomChunkBlock(FabricBlockSettings.of(Material.AIR));

    public static final Block SPAWN_CHUNK_BLOCK = new SpawnChunkBlock(TRIGGERED_SPAWN_CHUNK_BLOCK, FabricBlockSettings.of(Material.STONE));
    public static final Block UNSTABLE_SPAWN_CHUNK_BLOCK = new UnstableSpawnChunkBlock(FabricBlockSettings.of(Material.STONE));
    public static final Block BEDROCK_CHEST_BLOCK = new BedrockChestBlock(FabricBlockSettings.of(Material.STONE).strength(-1, 3600000.0F).noLootTable().isValidSpawn(((state, getter, pos, arg) -> false)));
    public static final Block WORLD_CORE_BLOCK = new Block(FabricBlockSettings.of(Material.STONE).strength(3.0F).lightLevel((state) -> 7));
    public static final Block WORLD_FORGE_BLOCK = new WorldForgeBlock(FabricBlockSettings.of(Material.STONE).strength(3.5F).lightLevel((state) -> 7));
    public static final Block WORLD_SCANNER_BLOCK = new WorldScannerBlock(FabricBlockSettings.of(Material.STONE).strength(3.5F).lightLevel((state) -> 4));

    public static Item SPAWN_CHUNK_BLOCK_ITEM;
    public static Item UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM;

    public static Item BEDROCK_CHEST_BLOCK_ITEM;
    public static Item WORLD_CORE_BLOCK_ITEM;
    public static Item WORLD_FORGE_BLOCK_ITEM;
    public static Item WORLD_SCANNER_BLOCK_ITEM;

    public static Item WORLD_FRAGMENT_ITEM;
    public static Item WORLD_SHARD_ITEM;
    public static Item WORLD_CRYSTAL_ITEM;

    public static BlockEntityType<BedrockChestBlockEntity> BEDROCK_CHEST_BLOCK_ENTITY;
    public static BlockEntityType<WorldForgeBlockEntity> WORLD_FORGE_BLOCK_ENTITY;
    public static BlockEntityType<WorldScannerBlockEntity> WORLD_SCANNER_BLOCK_ENTITY;
    public static BlockEntityType<TriggeredSpawnChunkBlockEntity> TRIGGERED_SPAWN_CHUNK_BLOCK_ENTITY;
    public static BlockEntityType<TriggeredSpawnRandomChunkBlockEntity> TRIGGERED_SPAWN_RANDOM_CHUNK_BLOCK_ENTITY;

    public static MenuType<BedrockChestMenu> BEDROCK_CHEST_MENU;
    public static MenuType<WorldForgeMenu> WORLD_FORGE_MENU;
    public static MenuType<WorldScannerMenu> WORLD_SCANNER_MENU;

    public static SoundEvent SPAWN_CHUNK_SOUND_EVENT;

    public static ResourceLocation CONFIG_PACKET = new ResourceLocation(ChunkByChunkConstants.MOD_ID, "config");

    static {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(ChunkByChunkConstants.MOD_ID, "scanner_data");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                ServerEventHandler.onResourceManagerReload(resourceManager);
            }
        });
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");

        Registry.register(Registry.BLOCK, createId("triggeredchunkspawner"), TRIGGERED_SPAWN_CHUNK_BLOCK);
        Registry.register(Registry.BLOCK, createId("triggeredrandomchunkspawner"), TRIGGERED_SPAWN_RANDOM_CHUNK_BLOCK);
        Registry.register(Registry.BLOCK, createId("chunkspawner"), SPAWN_CHUNK_BLOCK);
        Registry.register(Registry.BLOCK, createId("unstablechunkspawner"), UNSTABLE_SPAWN_CHUNK_BLOCK);

        Registry.register(Registry.BLOCK, createId("bedrockchest"), BEDROCK_CHEST_BLOCK);
        Registry.register(Registry.BLOCK, createId("worldcore"), WORLD_CORE_BLOCK);
        Registry.register(Registry.BLOCK, createId("worldforge"), WORLD_FORGE_BLOCK);
        Registry.register(Registry.BLOCK, createId("worldscanner"), WORLD_SCANNER_BLOCK);

        SPAWN_CHUNK_BLOCK_ITEM = Registry.register(Registry.ITEM, createId("chunkspawner"), new BlockItem(SPAWN_CHUNK_BLOCK, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
        UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM = Registry.register(Registry.ITEM, createId("unstablechunkspawner"), new BlockItem(UNSTABLE_SPAWN_CHUNK_BLOCK, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));

        List<Block> triggeredSpawnChunkEntityBlocks = new ArrayList<>();
        triggeredSpawnChunkEntityBlocks.add(TRIGGERED_SPAWN_CHUNK_BLOCK);
        for (ChunkByChunkConstants.BiomeTheme biomeGroup : ChunkByChunkConstants.OVERWORLD_BIOME_THEMES) {
            Block spawningBlock = new TriggeredBiomeSpawnChunkBlock(biomeGroup.name(), FabricBlockSettings.of(Material.AIR));
            Block spawnBlock = new SpawnChunkBlock(spawningBlock, FabricBlockSettings.of(Material.STONE));
            Registry.register(Registry.BLOCK, createId(biomeGroup.name() + ChunkByChunkConstants.BIOME_CHUNK_BlOCK_SUFFIX), spawnBlock);
            triggeredSpawnChunkEntityBlocks.add(Registry.register(Registry.BLOCK, createId(biomeGroup.name() + ChunkByChunkConstants.TRIGGERED_BIOME_CHUNK_BLOCK_SUFFIX), spawningBlock));
            Registry.register(Registry.ITEM, createId(biomeGroup.name() +  ChunkByChunkConstants.BIOME_CHUNK_BlOCK_ITEM_SUFFIX), new BlockItem(spawnBlock, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
        }

        BEDROCK_CHEST_BLOCK_ITEM = Registry.register(Registry.ITEM, createId("bedrockchest"), new BlockItem(BEDROCK_CHEST_BLOCK, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
        WORLD_CORE_BLOCK_ITEM = Registry.register(Registry.ITEM, createId("worldcore"), new BlockItem(WORLD_CORE_BLOCK, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
        WORLD_FORGE_BLOCK_ITEM = Registry.register(Registry.ITEM, createId("worldforge"), new BlockItem(WORLD_FORGE_BLOCK, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
        WORLD_SCANNER_BLOCK_ITEM = Registry.register(Registry.ITEM, createId("worldscanner"), new BlockItem(WORLD_SCANNER_BLOCK, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));

        WORLD_FRAGMENT_ITEM = Registry.register(Registry.ITEM, createId("worldfragment"), new Item(new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
        WORLD_SHARD_ITEM = Registry.register(Registry.ITEM, createId("worldshard"), new Item(new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
        WORLD_CRYSTAL_ITEM = Registry.register(Registry.ITEM, createId("worldcrystal"), new Item(new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));

        BEDROCK_CHEST_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, createId("bedrockchestentity"), FabricBlockEntityTypeBuilder.create(BedrockChestBlockEntity::new, BEDROCK_CHEST_BLOCK).build(null));
        WORLD_FORGE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, createId("worldforgeentity"), FabricBlockEntityTypeBuilder.create(WorldForgeBlockEntity::new, WORLD_FORGE_BLOCK).build(null));
        WORLD_SCANNER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, createId("worldscannerentity"), FabricBlockEntityTypeBuilder.create(WorldScannerBlockEntity::new, WORLD_SCANNER_BLOCK).build(null));
        TRIGGERED_SPAWN_CHUNK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, createId("triggeredspawnchunkentity"), FabricBlockEntityTypeBuilder.create(TriggeredSpawnChunkBlockEntity::new, triggeredSpawnChunkEntityBlocks.toArray(new Block[triggeredSpawnChunkEntityBlocks.size()])).build(null));
        TRIGGERED_SPAWN_RANDOM_CHUNK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, createId("triggeredspawnrandomchunkentity"), FabricBlockEntityTypeBuilder.create(TriggeredSpawnRandomChunkBlockEntity::new, TRIGGERED_SPAWN_RANDOM_CHUNK_BLOCK).build(null));

        BEDROCK_CHEST_MENU = ScreenHandlerRegistry.registerSimple(createId("bedrockchestmenu"), BedrockChestMenu::new);
        WORLD_FORGE_MENU = ScreenHandlerRegistry.registerSimple(createId("worldforgemenu"), WorldForgeMenu::new);
        WORLD_SCANNER_MENU = ScreenHandlerRegistry.registerSimple(createId("worldscannermenu"), WorldScannerMenu::new);

        SPAWN_CHUNK_SOUND_EVENT = Registry.register(Registry.SOUND_EVENT, createId("spawnchunkevent"), new SoundEvent(createId("chunk_spawn_sound")));

        Registry.register(Registry.CHUNK_GENERATOR, createId("skychunkgenerator"), SkyChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, createId("netherchunkgenerator"), SkyChunkGenerator.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(ServerEventHandler::onServerStarted);
        ServerLifecycleEvents.SERVER_STARTING.register(ServerEventHandler::onServerStarting);

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            SpawnChunkCommand.register(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            FriendlyByteBuf buffer = PacketByteBufs.create();
            buffer.writeBoolean(ChunkByChunkConfig.get().getGameplayConfig().isBlockPlacementAllowedOutsideSpawnedChunks());
            ServerPlayNetworking.send(handler.getPlayer(), CONFIG_PACKET, buffer);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockPos placePos = pos.relative(hitResult.getDirection());
            if (!CommonEventHandler.isBlockPlacementAllowed(placePos, player, world)) {
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        });

        setupConfig();
    }

    private void setupConfig() {
        new ConfigSystem().synchConfig(Paths.get("defaultconfigs", ChunkByChunkConstants.MOD_ID + ".toml"), ChunkByChunkConfig.get());
    }

    private ResourceLocation createId(String id) {
        return new ResourceLocation(ChunkByChunkConstants.MOD_ID, id);
    }


}