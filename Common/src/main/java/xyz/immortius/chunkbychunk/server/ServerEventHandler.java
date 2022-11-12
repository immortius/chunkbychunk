package xyz.immortius.chunkbychunk.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldScannerBlockEntity;
import xyz.immortius.chunkbychunk.common.data.ScannerData;
import xyz.immortius.chunkbychunk.common.data.SkyDimensionData;
import xyz.immortius.chunkbychunk.common.util.ChunkUtil;
import xyz.immortius.chunkbychunk.common.util.SpiralIterator;
import xyz.immortius.chunkbychunk.common.world.ChunkGeneratorAccess;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.common.world.SkyDimensions;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.config.system.ConfigSystem;
import xyz.immortius.chunkbychunk.interop.Services;
import xyz.immortius.chunkbychunk.mixins.ChunkGeneratorStructureAccessor;
import xyz.immortius.chunkbychunk.mixins.DefrostedRegistry;
import xyz.immortius.chunkbychunk.mixins.OverworldBiomeBuilderAccessor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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
            ChunkByChunkConstants.LOGGER.info("Setting up sky dimensions");
            applyChunkByChunkWorldGeneration(server);
        }
    }

    private static void applyChunkByChunkWorldGeneration(MinecraftServer server) {
        WorldGenSettings worldGenSettings = server.getWorldData().worldGenSettings();
        MappedRegistry<LevelStem> dimensions = (MappedRegistry<LevelStem>) worldGenSettings.dimensions();
        Registry<DimensionType> dimensionTypeRegistry = server.registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> biomeRegistry = server.registryAccess().registryOrThrow(BuiltinRegistries.BIOME.key());
        ((DefrostedRegistry) dimensions).setFrozen(false);

        for (Map.Entry<ResourceLocation, SkyDimensionData> entry : SkyDimensions.getSkyDimensions().entrySet()) {
            setupDimension(entry.getKey(), entry.getValue(), dimensions, dimensionTypeRegistry, biomeRegistry);
        }
        configureDimensionSynching(dimensions);

        ((DefrostedRegistry) dimensions).setFrozen(true);
    }

    private static void configureDimensionSynching(MappedRegistry<LevelStem> dimensions) {
        for (SkyDimensionData config : SkyDimensions.getSkyDimensions().values()) {
            if (!config.enabled) {
                continue;
            }

            LevelStem dimension = dimensions.get(new ResourceLocation(config.dimensionId));
            for (String synchDimId : config.synchToDimensions) {
                LevelStem synchDim =  dimensions.get(new ResourceLocation(synchDimId));
                if (DimensionType.getTeleportationScale(synchDim.typeHolder().value(), dimension.typeHolder().value()) > 1) {
                    ChunkByChunkConstants.LOGGER.warn("Cowardly refusing to synch dimension {} with {}, as the coordinate scale would result in a performance issues", config.dimensionId, synchDimId);
                    continue;
                }
                if (synchDim != null && synchDim.generator() instanceof SkyChunkGenerator generator) {
                    generator.addSynchLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(config.dimensionId)));
                } else {
                    ChunkByChunkConstants.LOGGER.warn("Cannot synch dimension {} with {}, as it is not a sky dimension", config.dimensionId, synchDimId);
                }

            }
        }
    }

    private static void setupDimension(ResourceLocation skyDimensionId, SkyDimensionData config, MappedRegistry<LevelStem> dimensions,  Registry<DimensionType> dimensionTypes, Registry<Biome> biomeRegistry) {
        if (!config.validate(skyDimensionId, dimensions)) {
            config.enabled = false;
        }
        if (!config.enabled) {
            return;
        }

        ChunkByChunkConstants.LOGGER.info("Setting up sky dimension for {}", config.dimensionId);

        LevelStem level = dimensions.get(new ResourceLocation(config.dimensionId));
        ChunkGenerator rootGenerator;
        if (level.generator() instanceof SkyChunkGenerator skyChunkGenerator) {
            rootGenerator = skyChunkGenerator.getParent();
        } else {
            rootGenerator = level.generator();
        }

        SkyChunkGenerator generator = setupCoreGenerationDimension(config, dimensions, dimensionTypes, level, rootGenerator);

        for (Map.Entry<String, List<String>> biomeTheme : config.biomeThemes.entrySet()) {
            ResourceKey<Level> biomeDim = setupThemeDimension(config.dimensionId, biomeTheme.getKey(), biomeTheme.getValue(), level, dimensions, rootGenerator, biomeRegistry);
            if (biomeDim != null) {
                generator.addBiomeDimension(biomeTheme.getKey(), biomeDim);
            }
        }
    }

    private static SkyChunkGenerator setupCoreGenerationDimension(SkyDimensionData config, MappedRegistry<LevelStem> dimensions, Registry<DimensionType> dimensionTypeRegistry, LevelStem level, ChunkGenerator rootGenerator) {
        ResourceLocation genDimensionId;
        if (config.genDimensionId == null || config.genDimensionId.isEmpty()) {
            genDimensionId = new ResourceLocation(config.dimensionId + "_gen");
        } else {
            genDimensionId = new ResourceLocation(config.genDimensionId);
        }

        ResourceKey<LevelStem> genLevelId = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, genDimensionId);
        LevelStem generationLevel = dimensions.get(genDimensionId);
        if (generationLevel == null) {
            generationLevel = new LevelStem(level.typeHolder(), rootGenerator);
            dimensions.register(genLevelId, generationLevel, Lifecycle.stable());
        }

        SkyChunkGenerator skyGenerator;
        if (!(level.generator() instanceof SkyChunkGenerator)) {
            skyGenerator = new SkyChunkGenerator(rootGenerator);
            LevelStem newLevelStem = new LevelStem(level.typeHolder(), skyGenerator);
            dimensions.registerOrOverride(OptionalInt.empty(), ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(config.dimensionId)), newLevelStem, Lifecycle.stable());
        } else {
            skyGenerator = (SkyChunkGenerator) level.generator();
        }
        skyGenerator.configure(ResourceKey.create(Registry.DIMENSION_REGISTRY, genLevelId.location()), config.generationType, config.initialChunks, config.allowChunkSpawner, config.allowUnstableChunkSpawner);
        return skyGenerator;
    }

    private static ResourceKey<Level> setupThemeDimension(String dimId, String themeName, List<String> biomes, LevelStem sourceLevel, MappedRegistry<LevelStem> dimensions, ChunkGenerator rootGenerator, Registry<Biome> biomeRegistry) {
        ResourceLocation biomeDimId = new ResourceLocation(dimId+ "_" + themeName + "_gen");
        LevelStem biomeLevel = dimensions.get(biomeDimId);

        if (biomeLevel == null) {
            List<ResourceKey<Biome>> biomeKeys = biomes.stream().map(x -> ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(x))).filter(key -> {
                boolean valid = biomeRegistry.containsKey(key);
                if (!valid) {
                    ChunkByChunkConstants.LOGGER.warn("Could not resolve biome {} for {}", key, dimId);
                }
                return valid;
            }).toList();

            ResourceKey<LevelStem> levelKey = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, biomeDimId);
            BiomeSource source;
            if (biomeKeys.size() == 0) {
                return null;
            } else if (biomeKeys.size() == 1) {
                source = new FixedBiomeSource(biomeRegistry.getHolderOrThrow(biomeKeys.get(0)));
            } else {
                source = new MultiNoiseBiomeSource.Preset(biomeDimId, biomeReg -> {
                    ImmutableList.Builder<Pair<Climate.ParameterPoint, Holder<Biome>>> builder = ImmutableList.builder();
                    ((OverworldBiomeBuilderAccessor)(Object) new OverworldBiomeBuilder()).callAddBiomes((pair) -> {
                        if (biomeKeys.contains(pair.getSecond())) {
                            builder.add(pair.mapSecond(biomeRegistry::getOrCreateHolderOrThrow));
                        }
                    });
                    return new Climate.ParameterList<>(builder.build());
                }).biomeSource(biomeRegistry);
            }
            biomeLevel = new LevelStem(sourceLevel.typeHolder(), new NoiseBasedChunkGenerator(((ChunkGeneratorStructureAccessor) rootGenerator).getStructureSet(), ChunkGeneratorAccess.getNoiseParamsRegistry(rootGenerator), source, ChunkGeneratorAccess.getNoiseGeneratorSettings(rootGenerator)));
            dimensions.register(levelKey, biomeLevel, Lifecycle.stable());
        }
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, biomeDimId);
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
        BlockPos overworldSpawnPos;
        if (overworldLevel != null && overworldLevel.getChunkSource().getGenerator() instanceof SkyChunkGenerator skyGenerator) {
            ServerLevel generationLevel = server.getLevel(skyGenerator.getGenerationLevel());
            overworldSpawnPos = generationLevel.getSharedSpawnPos();
            ChunkPos chunkSpawnPos = new ChunkPos(overworldSpawnPos);
            if (SpawnChunkHelper.isEmptyChunk(overworldLevel, chunkSpawnPos)) {
                overworldSpawnPos = findAppropriateSpawnChunk(overworldLevel, generationLevel);
                spawnInitialChunks(overworldLevel, skyGenerator.getInitialChunks(), overworldSpawnPos);
            }
        } else {
            overworldSpawnPos = overworldLevel.getSharedSpawnPos();
        }

        for (ServerLevel level : server.getAllLevels()) {
            if (level != overworldLevel && level.getChunkSource().getGenerator() instanceof SkyChunkGenerator levelGenerator) {
                if (levelGenerator.getInitialChunks() > 0) {
                    spawnInitialChunks(level, levelGenerator.getInitialChunks(), overworldSpawnPos);
                }
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
    private static BlockPos findAppropriateSpawnChunk(ServerLevel overworldLevel, ServerLevel generationLevel) {
        TagKey<Block> logsTag = BlockTags.LOGS;
        TagKey<Block> leavesTag = BlockTags.LEAVES;
        Set<Block> copper = ImmutableSet.of(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK);

        BlockPos spawnPos = overworldLevel.getSharedSpawnPos();
        ChunkPos initialChunkPos = new ChunkPos(spawnPos);
        SpiralIterator iterator = new SpiralIterator(initialChunkPos.x, initialChunkPos.z);
        int attempts = 0;
        while (attempts < MAX_FIND_CHUNK_ATTEMPTS) {
            LevelChunk chunk = generationLevel.getChunk(iterator.getX(), iterator.getY());
            if (ChunkUtil.countBlocks(chunk, logsTag) > 2
                    && ChunkUtil.countBlocks(chunk, Blocks.WATER) > 0
                    && ChunkUtil.countBlocks(chunk, leavesTag) > 3
                    && ChunkUtil.countBlocks(chunk, copper) >= 36) {
                ServerLevelData levelData = (ServerLevelData) overworldLevel.getLevelData();
                spawnPos = new BlockPos(chunk.getPos().getMiddleBlockX(), ChunkUtil.getSafeSpawnHeight(chunk, chunk.getPos().getMiddleBlockX(), chunk.getPos().getMiddleBlockZ()), chunk.getPos().getMiddleBlockZ());
                levelData.setSpawn(spawnPos, levelData.getSpawnAngle());
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
        return spawnPos;
    }

    /**
     * Spawns the initial chunks
     */
    private static void spawnInitialChunks(ServerLevel level, int initialChunks, BlockPos overworldSpawn) {
        BlockPos scaledSpawn = new BlockPos(Mth.floor(overworldSpawn.getX() / level.dimensionType().coordinateScale()), overworldSpawn.getY(), Mth.floor(overworldSpawn.getZ() / level.dimensionType().coordinateScale()));
        ChunkPos centerChunkPos = new ChunkPos(scaledSpawn);
        if (initialChunks <= CHUNK_SPAWN_OFFSETS.size()) {
            List<int[]> chunkOffsets = CHUNK_SPAWN_OFFSETS.get(initialChunks - 1);
            for (int[] offset : chunkOffsets) {
                ChunkPos targetPos = new ChunkPos(centerChunkPos.x + offset[0], centerChunkPos.z + offset[1]);
                SpawnChunkHelper.spawnChunkBlocks(level, targetPos);
                level.setBlock(new BlockPos(targetPos.getMiddleBlockX(), level.getMaxBuildHeight() - 1, targetPos.getMiddleBlockZ()), Services.PLATFORM.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_ALL);
            }
        } else {
            SpiralIterator spiralIterator = new SpiralIterator(centerChunkPos.x, centerChunkPos.z);
            for (int i = 0; i < initialChunks; i++) {
                ChunkPos targetPos = new ChunkPos(spiralIterator.getX(), spiralIterator.getY());
                level.setBlock(new BlockPos(targetPos.getMiddleBlockX(), level.getMaxBuildHeight() - 1, targetPos.getMiddleBlockZ()), Services.PLATFORM.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_ALL);
                spiralIterator.next();
            }
        }
    }

    public static void onResourceManagerReload(ResourceManager resourceManager) {
        Gson gson = new GsonBuilder().registerTypeAdapter(SkyChunkGenerator.EmptyGenerationType.class, (JsonDeserializer<SkyChunkGenerator.EmptyGenerationType>) (json, typeOfT, context) -> SkyChunkGenerator.EmptyGenerationType.getFromString(json.getAsString())).create();
        loadScannerData(resourceManager, gson);
        SkyDimensions.loadSkyDimensionData(resourceManager, gson);
    }

    private static void loadScannerData(ResourceManager resourceManager, Gson gson) {
        WorldScannerBlockEntity.clearItemMappings();
        int count = 0;
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(ChunkByChunkConstants.SCANNER_DATA_PATH, r -> true).entrySet()) {
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {
                ScannerData data = gson.fromJson(reader, ScannerData.class);
                data.process(entry.getKey());
                count++;
            } catch (IOException |RuntimeException e) {
                ChunkByChunkConstants.LOGGER.error("Failed to read scanner data '{}'", entry.getKey(), e);
            }
        }
        ChunkByChunkConstants.LOGGER.info("Loaded {} scanner data configs", count);
    }
}
