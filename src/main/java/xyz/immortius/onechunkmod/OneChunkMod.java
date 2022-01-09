package xyz.immortius.onechunkmod;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.onechunkmod.client.menus.BedrockChestMenu;
import xyz.immortius.onechunkmod.client.screens.BedrockChestScreen;
import xyz.immortius.onechunkmod.common.blockEntities.BedrockChestBlockEntity;
import xyz.immortius.onechunkmod.common.blocks.BedrockChestBlock;
import xyz.immortius.onechunkmod.common.blocks.SpawnChunkBlock;
import xyz.immortius.onechunkmod.common.world.SkyChunkGenerator;
import xyz.immortius.onechunkmod.common.world.SkyChunkGeneratorFactory;
import xyz.immortius.onechunkmod.common.world.SpawnChunkHelper;

@Mod("onechunkmod")
public class OneChunkMod {
    public static final String MOD_ID = "onechunkmod";
    public static final ResourceKey<Level> SKY_CHUNK_GENERATION_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "skychunkgeneration"));

    private static final DeferredRegister<ForgeWorldPreset> WORLD_PRESETS = DeferredRegister.create(ForgeRegistries.WORLD_TYPES, MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MOD_ID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    public static final RegistryObject<ForgeWorldPreset> ONE_CHUNK_WORLD = WORLD_PRESETS.register("onechunkskyworld", () -> new ForgeWorldPreset(new SkyChunkGeneratorFactory(false)));
    public static final RegistryObject<ForgeWorldPreset> SEALED_CHUNK_WORLD = WORLD_PRESETS.register("onechunksealedworld", () -> new ForgeWorldPreset(new SkyChunkGeneratorFactory(true)));
    public static final RegistryObject<Block> SPAWN_CHUNK_BLOCK = BLOCKS.register("chunkspawner", () -> new SpawnChunkBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Block> BEDROCK_CHEST_BLOCK = BLOCKS.register("bedrockchest", () -> new BedrockChestBlock(BlockBehaviour.Properties.of(Material.STONE).strength(-1, 3600000.0F).noDrops().isValidSpawn(((p_61031_, p_61032_, p_61033_, p_61034_) -> false))));
    public static final RegistryObject<Item> SPAWN_CHUNK_BLOCK_ITEM = ITEMS.register("chunkspawner", () -> new BlockItem(SPAWN_CHUNK_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> BEDROCK_CHEST_ITEM = ITEMS.register("bedrockchest", () -> new BlockItem(BEDROCK_CHEST_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<BlockEntityType<?>> BEDROCK_CHEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("bedrockchestentity", () -> BlockEntityType.Builder.of(BedrockChestBlockEntity::new, BEDROCK_CHEST_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<?>> BEDROCK_CHEST_MENU = CONTAINERS.register("bedrockchestmenu", () -> new MenuType<>(BedrockChestMenu::new));
    public static final RegistryObject<SoundEvent> SPAWN_CHUNK_SOUND_EVENT = SOUNDS.register("spawnchunkevent", () -> new SoundEvent(new ResourceLocation(MOD_ID, "chunk_spawn_sound")));

    static {
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(MOD_ID, "skychunkgenerator"), SkyChunkGenerator.CODEC);
    }

    public OneChunkMod() {
        WORLD_PRESETS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register((MenuType<BedrockChestMenu>) BEDROCK_CHEST_MENU.get(), BedrockChestScreen::new);
        });
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworldLevel = event.getServer().getLevel(Level.OVERWORLD);
        ServerLevel generationLevel = event.getServer().getLevel(SKY_CHUNK_GENERATION_LEVEL);

        if (overworldLevel != null && generationLevel != null) {
            BlockPos spawnPos = generationLevel.getSharedSpawnPos();
            ChunkPos chunkSpawnPos = new ChunkPos(spawnPos);
            if (SpawnChunkHelper.isEmptyChunk(overworldLevel, chunkSpawnPos)) {
                SpawnChunkHelper.spawnChunk(overworldLevel, chunkSpawnPos);
            }
        }
    }

}
