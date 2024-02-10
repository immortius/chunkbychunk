package xyz.immortius.chunkbychunk.fabric;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.CommonEventHandler;
import xyz.immortius.chunkbychunk.common.blockEntities.*;
import xyz.immortius.chunkbychunk.common.blocks.*;
import xyz.immortius.chunkbychunk.server.commands.SpawnChunkCommand;
import xyz.immortius.chunkbychunk.common.menus.BedrockChestMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldForgeMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldMenderMenu;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;
import xyz.immortius.chunkbychunk.server.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.config.system.ConfigSystem;
import xyz.immortius.chunkbychunk.server.ServerEventHandler;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Common mod initialization
 */
public class ChunkByChunkMod implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);

    public static final SpawnChunkBlock SPAWN_CHUNK_BLOCK = new SpawnChunkBlock("", false, FabricBlockSettings.of(Material.STONE));
    public static final Block UNSTABLE_SPAWN_CHUNK_BLOCK = new SpawnChunkBlock("", true, FabricBlockSettings.of(Material.STONE));
    public static final Block BEDROCK_CHEST_BLOCK = new BedrockChestBlock(FabricBlockSettings.of(Material.STONE).strength(-1, 3600000.0F).noLootTable().isValidSpawn(((state, getter, pos, arg) -> false)));
    public static final Block WORLD_CORE_BLOCK = new Block(FabricBlockSettings.of(Material.STONE).strength(3.0F).lightLevel((state) -> 7));
    public static final Block WORLD_FORGE_BLOCK = new WorldForgeBlock(FabricBlockSettings.of(Material.STONE).strength(3.5F).lightLevel((state) -> 7));
    public static final Block WORLD_SCANNER_BLOCK = new WorldScannerBlock(FabricBlockSettings.of(Material.STONE).strength(3.5F).lightLevel((state) -> 4));
    public static final Block WORLD_MENDER_BLOCK = new WorldMenderBlock(FabricBlockSettings.of(Material.STONE).strength(3.5F).lightLevel((state) -> 4));

    public static Item SPAWN_CHUNK_BLOCK_ITEM;
    public static Item UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM;

    public static Item BEDROCK_CHEST_BLOCK_ITEM;
    public static Item WORLD_CORE_BLOCK_ITEM;
    public static Item WORLD_FORGE_BLOCK_ITEM;
    public static Item WORLD_SCANNER_BLOCK_ITEM;
    public static Item WORLD_MENDER_BLOCK_ITEM;

    public static Item WORLD_FRAGMENT_ITEM;
    public static Item WORLD_SHARD_ITEM;
    public static Item WORLD_CRYSTAL_ITEM;

    public static BlockEntityType<BedrockChestBlockEntity> BEDROCK_CHEST_BLOCK_ENTITY;
    public static BlockEntityType<WorldForgeBlockEntity> WORLD_FORGE_BLOCK_ENTITY;
    public static BlockEntityType<WorldScannerBlockEntity> WORLD_SCANNER_BLOCK_ENTITY;
    public static BlockEntityType<WorldMenderBlockEntity> WORLD_MENDER_BLOCK_ENTITY;

    public static MenuType<BedrockChestMenu> BEDROCK_CHEST_MENU;
    public static MenuType<WorldForgeMenu> WORLD_FORGE_MENU;
    public static MenuType<WorldScannerMenu> WORLD_SCANNER_MENU;
    public static MenuType<WorldMenderMenu> WORLD_MENDER_MENU;

    public static SoundEvent SPAWN_CHUNK_SOUND_EVENT;

    public static ResourceLocation CONFIG_PACKET = new ResourceLocation(ChunkByChunkConstants.MOD_ID, "config");

    public static List<ItemStack> biomeThemedBlockItems;

    static {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(ChunkByChunkConstants.MOD_ID, "server_data");
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

        Registry.register(BuiltInRegistries.BLOCK, createId("chunkspawner"), SPAWN_CHUNK_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, createId("unstablechunkspawner"), UNSTABLE_SPAWN_CHUNK_BLOCK);

        Registry.register(BuiltInRegistries.BLOCK, createId("bedrockchest"), BEDROCK_CHEST_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, createId("worldcore"), WORLD_CORE_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, createId("worldforge"), WORLD_FORGE_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, createId("worldscanner"), WORLD_SCANNER_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, createId("worldmender"), WORLD_MENDER_BLOCK);

        SPAWN_CHUNK_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("chunkspawner"), new BlockItem(SPAWN_CHUNK_BLOCK, new FabricItemSettings()));
        UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("unstablechunkspawner"), new BlockItem(UNSTABLE_SPAWN_CHUNK_BLOCK, new FabricItemSettings()));

        List<ItemStack> themeSpawnBlockItems = new ArrayList<>();
        for (String biomeTheme : ChunkByChunkConstants.BIOME_THEMES) {
            Block spawnBlock = new SpawnChunkBlock(biomeTheme, false, FabricBlockSettings.of(Material.STONE));
            Registry.register(BuiltInRegistries.BLOCK, createId(biomeTheme + ChunkByChunkConstants.BIOME_CHUNK_BLOCK_SUFFIX), spawnBlock);
            BlockItem item = new BlockItem(spawnBlock, new FabricItemSettings());
            Registry.register(BuiltInRegistries.ITEM, createId(biomeTheme +  ChunkByChunkConstants.BIOME_CHUNK_BLOCK_ITEM_SUFFIX), item);
            themeSpawnBlockItems.add(item.getDefaultInstance());
        }
        biomeThemedBlockItems = ImmutableList.copyOf(themeSpawnBlockItems);

        BEDROCK_CHEST_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("bedrockchest"), new BlockItem(BEDROCK_CHEST_BLOCK, new FabricItemSettings()));
        WORLD_CORE_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("worldcore"), new BlockItem(WORLD_CORE_BLOCK, new FabricItemSettings()));
        WORLD_FORGE_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("worldforge"), new BlockItem(WORLD_FORGE_BLOCK, new FabricItemSettings()));
        WORLD_SCANNER_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("worldscanner"), new BlockItem(WORLD_SCANNER_BLOCK, new FabricItemSettings()));
        WORLD_MENDER_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("worldmender"), new BlockItem(WORLD_MENDER_BLOCK, new FabricItemSettings()));

        WORLD_FRAGMENT_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("worldfragment"), new Item(new FabricItemSettings()));
        WORLD_SHARD_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("worldshard"), new Item(new FabricItemSettings()));
        WORLD_CRYSTAL_ITEM = Registry.register(BuiltInRegistries.ITEM, createId("worldcrystal"), new Item(new FabricItemSettings()));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.addAfter(Items.MUSIC_DISC_PIGSTEP, WORLD_FRAGMENT_ITEM, WORLD_SHARD_ITEM, WORLD_CRYSTAL_ITEM, WORLD_CORE_BLOCK_ITEM, WORLD_FORGE_BLOCK_ITEM, WORLD_SCANNER_BLOCK_ITEM, WORLD_MENDER_BLOCK_ITEM, SPAWN_CHUNK_BLOCK_ITEM, UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM);
            content.addAfter(UNSTABLE_SPAWN_CHUNK_BLOCK_ITEM, themeSpawnBlockItems.toArray(new ItemStack[]{}));
        });

        BEDROCK_CHEST_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, createId("bedrockchestentity"), FabricBlockEntityTypeBuilder.create(BedrockChestBlockEntity::new, BEDROCK_CHEST_BLOCK).build(null));
        WORLD_FORGE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, createId("worldforgeentity"), FabricBlockEntityTypeBuilder.create(WorldForgeBlockEntity::new, WORLD_FORGE_BLOCK).build(null));
        WORLD_SCANNER_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, createId("worldscannerentity"), FabricBlockEntityTypeBuilder.create(WorldScannerBlockEntity::new, WORLD_SCANNER_BLOCK).build(null));
        WORLD_MENDER_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, createId("worldmenderentity"), FabricBlockEntityTypeBuilder.create(WorldMenderBlockEntity::new, WORLD_MENDER_BLOCK).build(null));

        BEDROCK_CHEST_MENU = ScreenHandlerRegistry.registerSimple(createId("bedrockchestmenu"), BedrockChestMenu::new);
        WORLD_FORGE_MENU = ScreenHandlerRegistry.registerSimple(createId("worldforgemenu"), WorldForgeMenu::new);
        WORLD_SCANNER_MENU = ScreenHandlerRegistry.registerSimple(createId("worldscannermenu"), WorldScannerMenu::new);
        WORLD_MENDER_MENU = ScreenHandlerRegistry.registerSimple(createId("worldmendermenu"), WorldMenderMenu::new);

        SPAWN_CHUNK_SOUND_EVENT = Registry.register(BuiltInRegistries.SOUND_EVENT, createId("spawnchunkevent"), SoundEvent.createVariableRangeEvent(createId("chunk_spawn_sound")));

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, createId("skychunkgenerator"), SkyChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, createId("netherchunkgenerator"), SkyChunkGenerator.OLD_NETHER_CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(ServerEventHandler::onServerStarted);
        ServerLifecycleEvents.SERVER_STARTING.register(ServerEventHandler::onServerStarting);
        ServerTickEvents.END_SERVER_TICK.register(ServerEventHandler::onLevelTick);

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