package xyz.immortius.chunkbychunk.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.common.util.ChunkUtil;
import xyz.immortius.chunkbychunk.common.util.SpiralIterator;
import xyz.immortius.chunkbychunk.common.world.NetherChunkByChunkGenerator;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.config.system.ConfigSystem;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Server event handlers for events triggered server-side
 */
public final class ServerEventHandler {

    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);
    private static final int MAX_FIND_CHUNK_ATTEMPTS = 512;
    private static final String SERVERCONFIG = "serverconfig";
    private static final ConfigSystem configSystem = new ConfigSystem();

    private static final List<List<int[]>> CHUNK_SPAWN_OFFSETS = ImmutableList.<List<int[]>>builder()
            .add(ImmutableList.of(new int[]{0, 0}))
            .add(ImmutableList.of(new int[]{0, 0}, new int[]{1, 0}))
            .add(ImmutableList.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{0, 1}))
            .add(ImmutableList.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{0, 1}, new int[]{1, 1}))
            .add(ImmutableList.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{0, 1}, new int[]{-1, 0}, new int[]{0, -1}))
            .add(ImmutableList.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{0, 1}, new int[]{-1, 0}, new int[]{0, -1}, new int[]{1, 1}))
            .add(ImmutableList.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{0, 1}, new int[]{-1, 0}, new int[]{0, -1}, new int[]{1, 1}, new int[]{-1, -1}))
            .add(ImmutableList.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{0, 1}, new int[]{-1, 0}, new int[]{0, -1}, new int[]{1, 1}, new int[]{-1, -1}, new int[]{1, -1}))
            .add(ImmutableList.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{0, 1}, new int[]{-1, 0}, new int[]{0, -1}, new int[]{1, 1}, new int[]{-1, -1}, new int[]{1, -1}, new int[]{-1, 1}))
            .build();

    private ServerEventHandler() {

    }

    /**
     * Handles the event when the server is first starting, before any levels are created. Used to fiddle the world generators to move the existing overworld generation into a new generation dimension, and change
     * the overworld to spawn as a skyworld.
     * @param server The minecraft server that is starting
     */
    public static void onServerStarting(MinecraftServer server) {
        configSystem.synchConfig(server.getWorldPath(LevelResource.ROOT).resolve(SERVERCONFIG).resolve(ChunkByChunkConstants.CONFIG_FILE), ChunkByChunkConfig.get());
        if (ChunkByChunkConfig.get().getGeneration().isEnabled()) {
            applyChunkByChunkWorldGeneration(server);
        }

    }

    private static void applyChunkByChunkWorldGeneration(MinecraftServer server) {
        WorldGenSettings worldGenSettings = server.getWorldData().worldGenSettings();
        LevelStem overworldStem = worldGenSettings.dimensions().get(Level.OVERWORLD.location());
        LevelStem generationStem = worldGenSettings.dimensions().get(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL.location());
        WritableRegistry<LevelStem> dimensions = (WritableRegistry<LevelStem>) worldGenSettings.dimensions();
        if (!(overworldStem.generator() instanceof SkyChunkGenerator)) {
            Registry<DimensionType> dimensionTypeRegistry = server.registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);

            if (generationStem == null) {
                ResourceKey<LevelStem> skychunkgeneration = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL.location());
                generationStem = new LevelStem(dimensionTypeRegistry.getOrCreateHolder(DimensionType.OVERWORLD_LOCATION), overworldStem.generator());
                dimensions.register(skychunkgeneration, generationStem, Lifecycle.stable());
            }

            LevelStem newOverworldStem = new LevelStem(dimensionTypeRegistry.getOrCreateHolder(DimensionType.OVERWORLD_LOCATION), new SkyChunkGenerator(overworldStem.generator(), ChunkByChunkConfig.get().getGeneration().sealWorld()));
            dimensions.registerOrOverride(OptionalInt.empty(), ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, Level.OVERWORLD.location()), newOverworldStem, Lifecycle.stable());
        }
        if (ChunkByChunkConfig.get().getGeneration().isSynchNether()) {
            LevelStem netherStem = worldGenSettings.dimensions().get(Level.NETHER.location());
            if (!(netherStem.generator() instanceof NetherChunkByChunkGenerator)) {
                LevelStem netherGenerationStem = worldGenSettings.dimensions().get(ChunkByChunkConstants.NETHER_CHUNK_GENERATION_LEVEL.location());
                Registry<DimensionType> dimensionTypeRegistry = server.registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);

                if (netherGenerationStem == null) {
                    ResourceKey<LevelStem> netherchunkgeneration = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, ChunkByChunkConstants.NETHER_CHUNK_GENERATION_LEVEL.location());
                    netherGenerationStem = new LevelStem(dimensionTypeRegistry.getOrCreateHolder(DimensionType.NETHER_LOCATION), netherStem.generator());
                    dimensions.register(netherchunkgeneration, netherGenerationStem, Lifecycle.stable());
                }

                LevelStem newNetherStem = new LevelStem(dimensionTypeRegistry.getOrCreateHolder(DimensionType.NETHER_LOCATION), new NetherChunkByChunkGenerator(netherStem.generator()));
                dimensions.registerOrOverride(OptionalInt.empty(), ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, Level.NETHER.location()), newNetherStem, Lifecycle.stable());
            }
        }
    }

    /**
     * Event when the server has started. Loads/synchs the server config and spawns the initial chunk if needed.
     * @param server The minecraft server that has started
     */
    public static void onServerStarted(MinecraftServer server) {
        if (ChunkByChunkConfig.get().getGeneration().isEnabled()) {
            checkSpawnInitialChunks(server);
        }
    }

    private static void checkSpawnInitialChunks(MinecraftServer server) {
        ServerLevel overworldLevel = server.getLevel(Level.OVERWORLD);
        ServerLevel generationLevel = server.getLevel(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL);

        if (overworldLevel != null && generationLevel != null) {
            BlockPos spawnPos = generationLevel.getSharedSpawnPos();
            ChunkPos chunkSpawnPos = new ChunkPos(spawnPos);
            if (SpawnChunkHelper.isEmptyChunk(overworldLevel, chunkSpawnPos)) {
                findAppropriateSpawnChunk(overworldLevel, generationLevel);
                spawnInitialChunks(overworldLevel);
            }
        }
    }

    /**
     * Finds an appropriate spawn chunk. I want it to have at least 2 logs and 2 leaves, and mats to make another chunk...
     * Obviously this isn't necessarily sufficient because if no seeds drop then the
     * player is in trouble, but provides some baseline threshold for an acceptable chunk.
     * @param overworldLevel
     * @param generationLevel
     */
    private static void findAppropriateSpawnChunk(ServerLevel overworldLevel, ServerLevel generationLevel) {
        TagKey<Block> logsTag = BlockTags.LOGS;
        TagKey<Block> leavesTag = BlockTags.LEAVES;
        Set<Block> redstone = ImmutableSet.of(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        Set<Block> copper = ImmutableSet.of(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK);

        ChunkPos initialChunkPos = new ChunkPos(overworldLevel.getSharedSpawnPos());
        SpiralIterator iterator = new SpiralIterator(initialChunkPos.x, initialChunkPos.z);
        int attempts = 0;
        while (attempts < MAX_FIND_CHUNK_ATTEMPTS) {
            LevelChunk chunk = generationLevel.getChunk(iterator.getX(), iterator.getY());
            if (ChunkUtil.countBlocks(chunk, logsTag) > 1 && ChunkUtil.countBlocks(chunk, leavesTag) > 1 && ChunkUtil.countBlocks(chunk, redstone) >= 36 && ChunkUtil.countBlocks(chunk, copper) >= 36) {
                ServerLevelData levelData = (ServerLevelData) overworldLevel.getLevelData();
                levelData.setSpawn(new BlockPos(chunk.getPos().getMiddleBlockX(), ChunkUtil.getSafeSpawnHeight(chunk, chunk.getPos().getMiddleBlockX(), chunk.getPos().getMiddleBlockZ()), chunk.getPos().getMiddleBlockZ()), levelData.getSpawnAngle());
                break;
            }
            iterator.next();
            attempts++;
        }
        if (attempts < MAX_FIND_CHUNK_ATTEMPTS) {
            LOGGER.info("Found appropriate spawn chunk in {} attempts", attempts);
        } else {
            LOGGER.info("No appropriate spawn chunk found :(");
        }
    }

    /**
     * Spawns the initial chunk.
     * @param overworldLevel
     */
    private static void spawnInitialChunks(ServerLevel overworldLevel) {
        ChunkPos centerChunkPos = new ChunkPos(overworldLevel.getSharedSpawnPos());
        List<int[]> chunkOffsets = CHUNK_SPAWN_OFFSETS.get(ChunkByChunkConfig.get().getGeneration().getInitialChunks() - 1);
        for (int[] offset : chunkOffsets) {
            ChunkPos targetPos = new ChunkPos(centerChunkPos.x + offset[0], centerChunkPos.z + offset[1]);
            SpawnChunkHelper.spawnChunkBlocks(overworldLevel, targetPos);
            overworldLevel.setBlock(new BlockPos(targetPos.getMiddleBlockX(), overworldLevel.getMaxBuildHeight() - 1, targetPos.getMiddleBlockZ()), ChunkByChunkConstants.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
