package xyz.immortius.onechunkmod;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
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
import xyz.immortius.onechunkmod.common.world.SkyChunkCopyGenerator;
import xyz.immortius.onechunkmod.common.world.SkyChunkGeneratorFactory;
import xyz.immortius.onechunkmod.common.world.SkyChunkPrimeGenerator;
import xyz.immortius.onechunkmod.common.world.SpawnChunkHelper;

import java.util.stream.Collectors;

@Mod("onechunkmod")
public class OneChunkMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "onechunkmod";
    public static final ResourceKey<Level> SKY_CHUNK_GENERATION_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "skychunkgeneration"));

    private static final DeferredRegister<ForgeWorldPreset> WORLD_PRESETS = DeferredRegister.create(ForgeRegistries.WORLD_TYPES, MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MOD_ID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);


    public static final RegistryObject<ForgeWorldPreset> ONE_CHUNK_WORLD = WORLD_PRESETS.register("onechunkskyworld", ()-> new ForgeWorldPreset(new SkyChunkGeneratorFactory()));
    public static final RegistryObject<Block> SPAWN_CHUNK_BLOCK = BLOCKS.register("chunkspawner", () -> new SpawnChunkBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Block> BEDROCK_CHEST_BLOCK = BLOCKS.register("bedrockchest", () -> new BedrockChestBlock(BlockBehaviour.Properties.of(Material.STONE).strength(-1, 3600000.0F).noDrops().isValidSpawn(((p_61031_, p_61032_, p_61033_, p_61034_) -> false))));
    public static final RegistryObject<Item> SPAWN_CHUNK_BLOCK_ITEM = ITEMS.register("chunkspawner", () -> new BlockItem(SPAWN_CHUNK_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> BEDROCK_CHEST_ITEM = ITEMS.register("bedrockchest", () -> new BlockItem(BEDROCK_CHEST_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<BlockEntityType<?>> BEDROCK_CHEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("bedrockchestentity", () -> BlockEntityType.Builder.of(BedrockChestBlockEntity::new, BEDROCK_CHEST_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<?>> BEDROCK_CHEST_MENU = CONTAINERS.register("bedrockchestmenu", () -> new MenuType<>(BedrockChestMenu::new));

    static {
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(MOD_ID, "skychunkprime"), SkyChunkPrimeGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(MOD_ID, "skychunkcopy"), SkyChunkCopyGenerator.CODEC);
    }

    public OneChunkMod() {
        WORLD_PRESETS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            MenuScreens.register((MenuType<BedrockChestMenu>) BEDROCK_CHEST_MENU.get(), BedrockChestScreen::new);
        });
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo(MOD_ID, "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Server started");
        event.getServer().levelKeys().forEach(x -> {
            LOGGER.info("Level: {}", x.location());
        });
        ServerLevel overworldLevel = event.getServer().getLevel(Level.OVERWORLD);
        ServerLevel copyLevel = event.getServer().getLevel(SKY_CHUNK_GENERATION_LEVEL);

        if (overworldLevel != null && copyLevel != null) {
            BlockPos spawnPos = copyLevel.getSharedSpawnPos();
            ChunkPos chunkSpawnPos = new ChunkPos(spawnPos);
            if (SpawnChunkHelper.isEmptyChunk(overworldLevel, chunkSpawnPos)) {
                SpawnChunkHelper.spawnChunk(overworldLevel, chunkSpawnPos);
            }
        }
    }

}
