package xyz.immortius.chunkbychunk.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.common.util.ChunkUtil;
import xyz.immortius.chunkbychunk.common.util.SpiralIterator;
import xyz.immortius.chunkbychunk.common.world.*;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.config.system.ConfigSystem;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.interop.Services;
import xyz.immortius.chunkbychunk.mixins.ChunkGeneratorStructureAccessor;
import xyz.immortius.chunkbychunk.mixins.DefrostedRegistry;
import xyz.immortius.chunkbychunk.mixins.NoiseBasedChunkGeneratorMixin;
import xyz.immortius.chunkbychunk.mixins.OverworldBiomeBuilderAccessor;

import java.util.Arrays;
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
        MappedRegistry<LevelStem> dimensions = (MappedRegistry<LevelStem>) worldGenSettings.dimensions();
        Registry<DimensionType> dimensionTypeRegistry = server.registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        ((DefrostedRegistry) dimensions).setFrozen(false);

        LevelStem overworldLevel = worldGenSettings.dimensions().get(Level.OVERWORLD.location());
        ChunkGenerator rootOverworldGenerator;
        if (overworldLevel.generator() instanceof SkyChunkGenerator skyChunkGenerator) {
            rootOverworldGenerator = skyChunkGenerator.getParent();
        } else {
            rootOverworldGenerator = overworldLevel.generator();
        }

        setupOverworldGeneration(worldGenSettings, dimensions, dimensionTypeRegistry, overworldLevel, rootOverworldGenerator);

        for (ChunkByChunkConstants.BiomeTheme biomeGroup : ChunkByChunkConstants.OVERWORLD_BIOME_THEMES) {
            setupThemeDimension(server, worldGenSettings, dimensions, dimensionTypeRegistry, rootOverworldGenerator, biomeGroup);
        }

        if (ChunkByChunkConfig.get().getGeneration().isSynchNether()) {
            setupNetherGeneration(worldGenSettings, dimensions, dimensionTypeRegistry);
        }
        ((DefrostedRegistry) dimensions).setFrozen(true);
    }

    private static void setupNetherGeneration(WorldGenSettings worldGenSettings, MappedRegistry<LevelStem> dimensions, Registry<DimensionType> dimensionTypeRegistry) {
        LevelStem netherStem = worldGenSettings.dimensions().get(Level.NETHER.location());
        LevelStem netherGenerationStem = worldGenSettings.dimensions().get(ChunkByChunkConstants.NETHER_CHUNK_GENERATION_LEVEL.location());

        if (!(netherStem.generator() instanceof NetherChunkByChunkGenerator)) {
            if (netherGenerationStem == null) {
                ResourceKey<LevelStem> netherchunkgeneration = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, ChunkByChunkConstants.NETHER_CHUNK_GENERATION_LEVEL.location());
                netherGenerationStem = new LevelStem(dimensionTypeRegistry.getHolderOrThrow(BuiltinDimensionTypes.NETHER), netherStem.generator());
                dimensions.register(netherchunkgeneration, netherGenerationStem, Lifecycle.stable());
            }

            LevelStem newNetherStem = new LevelStem(dimensionTypeRegistry.getHolderOrThrow(BuiltinDimensionTypes.NETHER), new NetherChunkByChunkGenerator(netherStem.generator()));
            dimensions.registerOrOverride(OptionalInt.empty(), ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, Level.NETHER.location()), newNetherStem, Lifecycle.stable());
        } else if (netherGenerationStem == null) {
            ResourceKey<LevelStem> netherchunkgeneration = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, ChunkByChunkConstants.NETHER_CHUNK_GENERATION_LEVEL.location());
            netherGenerationStem = new LevelStem(dimensionTypeRegistry.getHolderOrThrow(BuiltinDimensionTypes.NETHER), ((BaseSkyChunkGenerator) netherStem.generator()).getParent());
            dimensions.register(netherchunkgeneration, netherGenerationStem, Lifecycle.stable());
        }
    }

    private static void setupOverworldGeneration(WorldGenSettings worldGenSettings, MappedRegistry<LevelStem> dimensions, Registry<DimensionType> dimensionTypeRegistry, LevelStem overworldLevel, ChunkGenerator rootOverworldGenerator) {
        LevelStem overworldGenerationLevel = worldGenSettings.dimensions().get(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL.location());
        if (overworldGenerationLevel == null) {
            ResourceKey<LevelStem> skychunkgeneration = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL.location());
            overworldGenerationLevel = new LevelStem(dimensionTypeRegistry.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD), rootOverworldGenerator);
            dimensions.register(skychunkgeneration, overworldGenerationLevel, Lifecycle.stable());
        }
        if (!(overworldLevel.generator() instanceof SkyChunkGenerator)) {
            LevelStem newOverworldStem = new LevelStem(dimensionTypeRegistry.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD), new SkyChunkGenerator(overworldLevel.generator(), ChunkByChunkConfig.get().getGeneration().sealWorld()));
            dimensions.registerOrOverride(OptionalInt.empty(), ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, Level.OVERWORLD.location()), newOverworldStem, Lifecycle.stable());
        }
    }

    private static void setupThemeDimension(MinecraftServer server, WorldGenSettings worldGenSettings, MappedRegistry<LevelStem> dimensions, Registry<DimensionType> dimensionTypeRegistry, ChunkGenerator rootOverworldGenerator, ChunkByChunkConstants.BiomeTheme biomeTheme) {
        ResourceLocation dimLocation = new ResourceLocation(ChunkByChunkConstants.MOD_ID, biomeTheme.name() + ChunkByChunkConstants.BIOME_CHUNK_GENERATION_LEVEL_SUFFIX);
        LevelStem biomeLevel = worldGenSettings.dimensions().get(dimLocation);
        if (biomeLevel == null) {
            ResourceKey<LevelStem> key = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, dimLocation);
            BiomeSource source;
            if (biomeTheme.biomes().length == 1) {
                source = new FixedBiomeSource(server.registryAccess().registry(BuiltinRegistries.BIOME.key()).get().getHolderOrThrow(biomeTheme.biomes()[0]));
            } else {
                source = new MultiNoiseBiomeSource.Preset(new ResourceLocation(ChunkByChunkConstants.MOD_ID, biomeTheme.name() + "biomesource"), biomeRegistry -> {
                    ImmutableList.Builder<Pair<Climate.ParameterPoint, Holder<Biome>>> builder = ImmutableList.builder();
                    ((OverworldBiomeBuilderAccessor)(Object) new OverworldBiomeBuilder()).callAddBiomes((pair) -> {
                        if (Arrays.asList(biomeTheme.biomes()).contains(pair.getSecond())) {
                            builder.add(pair.mapSecond(biomeRegistry::getOrCreateHolderOrThrow));
                        }
                    });
                    return new Climate.ParameterList<>(builder.build());
                }).biomeSource(server.registryAccess().registry(BuiltinRegistries.BIOME.key()).get());
            }
            biomeLevel = new LevelStem(dimensionTypeRegistry.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD), new NoiseBasedChunkGenerator(((ChunkGeneratorStructureAccessor) rootOverworldGenerator).getStructureSet(), ChunkGeneratorAccess.getNoiseParamsRegistry(rootOverworldGenerator), source, ChunkGeneratorAccess.getNoiseGeneratorSettings(rootOverworldGenerator)));

            dimensions.register(key, biomeLevel, Lifecycle.stable());
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
        Set<Block> copper = ImmutableSet.of(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK);

        ChunkPos initialChunkPos = new ChunkPos(overworldLevel.getSharedSpawnPos());
        SpiralIterator iterator = new SpiralIterator(initialChunkPos.x, initialChunkPos.z);
        int attempts = 0;
        while (attempts < MAX_FIND_CHUNK_ATTEMPTS) {
            LevelChunk chunk = generationLevel.getChunk(iterator.getX(), iterator.getY());
            if (ChunkUtil.countBlocks(chunk, logsTag) > 2
                    && ChunkUtil.countBlocks(chunk, Blocks.WATER) > 0
                    && ChunkUtil.countBlocks(chunk, leavesTag) > 3
                    && ChunkUtil.countBlocks(chunk, copper) >= 36) {
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
            overworldLevel.setBlock(new BlockPos(targetPos.getMiddleBlockX(), overworldLevel.getMaxBuildHeight() - 1, targetPos.getMiddleBlockZ()), Services.PLATFORM.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
