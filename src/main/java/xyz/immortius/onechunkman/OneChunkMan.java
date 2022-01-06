package xyz.immortius.onechunkman;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import xyz.immortius.onechunkman.common.blocks.SpawnChunkBlock;
import xyz.immortius.onechunkman.common.world.EmptyGenerator;
import xyz.immortius.onechunkman.common.world.OneChunkGenerator;
import xyz.immortius.onechunkman.common.world.OneChunkGeneratorFactory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

@Mod("onechunkman")
public class OneChunkMan
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private static final DeferredRegister<ForgeWorldPreset> WORLD_PRESETS = DeferredRegister.create(ForgeRegistries.WORLD_TYPES, "onechunkman");
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "onechunkman");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "onechunkman");
    public static final RegistryObject<ForgeWorldPreset> ONE_CHUNK_WORLD = WORLD_PRESETS.register("onechunkskyworld", ()-> new ForgeWorldPreset(new OneChunkGeneratorFactory()));
    public static final RegistryObject<Block> SPAWN_CHUNK_BLOCK = BLOCKS.register("chunkspawner", () -> new SpawnChunkBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> SPAWN_CHUNK_BLOCK_ITEM = ITEMS.register("chunkspawner", () -> new BlockItem(SPAWN_CHUNK_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    static {
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation("onechunkman", "onechunk"), OneChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation("onechunkman", "empty"), EmptyGenerator.CODEC);
    }

    public OneChunkMan() {

        LOGGER.info("Registering world preset");
        // Register world preset
        WORLD_PRESETS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
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

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("onechunkman", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
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
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getPlayer() instanceof ServerPlayer serverPlayer) {
            ServerLevel overworldLevel = serverPlayer.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("minecraft", "overworld")));
            ServerLevel emptyLevel = serverPlayer.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("onechunkman", "empty")));

            if (overworldLevel.equals(serverPlayer.getLevel())) {
                serverPlayer.teleportTo(emptyLevel, serverPlayer.position().x, serverPlayer.position().y, serverPlayer.position().z, serverPlayer.getYRot(), serverPlayer.getXRot());
                serverPlayer.setRespawnPosition(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("onechunkman", "empty")), serverPlayer.blockPosition(), serverPlayer.getRespawnAngle(), false, true);
                LOGGER.info("Teleported player {} to empty", serverPlayer.getDisplayName().getString());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getPlayer() instanceof ServerPlayer serverPlayer) {
            ServerLevel overworldLevel = serverPlayer.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("minecraft", "overworld")));
            ServerLevel emptyLevel = serverPlayer.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("onechunkman", "empty")));

            if (overworldLevel.equals(serverPlayer.getLevel())) {
                serverPlayer.teleportTo(emptyLevel, serverPlayer.position().x, serverPlayer.position().y, serverPlayer.position().z, serverPlayer.getYRot(), serverPlayer.getXRot());
                serverPlayer.setRespawnPosition(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("onechunkman", "empty")), serverPlayer.blockPosition(), serverPlayer.getRespawnAngle(), false, true);
                LOGGER.info("Teleported player {} to empty", serverPlayer.getDisplayName().getString());
            }
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Server started");
        event.getServer().levelKeys().forEach(x -> {
            LOGGER.info("Level: {}", x.location());
        });
        ServerLevel overworldLevel = event.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("minecraft", "overworld")));
        ServerLevel emptyLevel = event.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("onechunkman", "empty")));

        BlockPos spawnPos = overworldLevel.getSharedSpawnPos();
        ChunkAccess spawnChunk = emptyLevel.getChunk(spawnPos);
        if (isEmptyChunk(spawnChunk)) {
            populateChunk(overworldLevel, emptyLevel, new ChunkPos(spawnPos));
        }

    }

    private void populateChunk(ServerLevel from, ServerLevel to, ChunkPos chunkPos) {
        BlockPos.betweenClosed(chunkPos.getMinBlockX(), from.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), from.getMaxBuildHeight(), chunkPos.getMaxBlockZ()).forEach(pos -> {
            to.setBlock(pos, from.getBlockState(pos), Block.UPDATE_ALL);
        });
    }

    private static boolean isEmptyChunk(ChunkAccess chunk) {
        return !Blocks.BEDROCK.equals(chunk.getBlockState(new BlockPos(8,chunk.getMinBuildHeight(),8)).getBlock());
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
