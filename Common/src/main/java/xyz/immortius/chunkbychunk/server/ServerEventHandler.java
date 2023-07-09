package xyz.immortius.chunkbychunk.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
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
import net.minecraft.world.level.levelgen.structure.Structure;
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
import xyz.immortius.chunkbychunk.server.world.*;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.config.system.ConfigSystem;
import xyz.immortius.chunkbychunk.interop.Services;
import xyz.immortius.chunkbychunk.mixins.DefrostedRegistry;
import xyz.immortius.chunkbychunk.mixins.OverworldBiomeBuilderAccessor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
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
        configSystem.synchConfig(server.getWorldPath(LevelResource.ROOT).resolve(SERVERCONFIG).resolve(ChunkByChunkConstants.CONFIG_FILE), Paths.get(ChunkByChunkConstants.DEFAULT_CONFIG_PATH).resolve(ChunkByChunkConstants.CONFIG_FILE), ChunkByChunkConfig.get());
        if (ChunkByChunkConfig.get().getGeneration().isEnabled()) {
            ChunkByChunkConstants.LOGGER.info("Setting up sky dimensions");
            applySkyDimensionConfig(server.registryAccess());
            applyChunkByChunkWorldGeneration(server);
        }
    }

    private static void applySkyDimensionConfig(RegistryAccess registryAccess) {
        if (ChunkByChunkConfig.get().getGeneration().isSynchNether()) {
            SkyDimensions.getSkyDimensions().values().stream().filter(x -> "minecraft:the_nether".equals(x.dimensionId) || "the_nether".equals(x.dimensionId)).forEach(x -> {
                x.synchToDimensions.add("minecraft:overworld");
            });
        }
        if (ChunkByChunkConfig.get().getGeneration().sealWorld()) {
            SkyDimensions.getSkyDimensions().values().stream().filter(x -> "minecraft:overworld".equals(x.dimensionId) || "overworld".equals(x.dimensionId)).forEach(x -> {
                x.generationType = SkyChunkGenerator.EmptyGenerationType.Sealed;
            });
        }
        if (ChunkByChunkConfig.get().getGeneration().getInitialChunks() != 1) {
            SkyDimensions.getSkyDimensions().values().stream().filter(x -> "minecraft:overworld".equals(x.dimensionId) || "overworld".equals(x.dimensionId)).forEach(x -> {
                x.initialChunks = ChunkByChunkConfig.get().getGeneration().getInitialChunks();
            });
        }
    }

    private static void applyChunkByChunkWorldGeneration(MinecraftServer server) {
        MappedRegistry<LevelStem> dimensions = (MappedRegistry<LevelStem>) server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        MappedRegistry<Biome> biomeRegistry = (MappedRegistry<Biome>) server.registryAccess().registryOrThrow(Registries.BIOME);
        Registry<DimensionType> dimensionTypeRegistry = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);
        Registry<Block> blocks = server.registryAccess().registry(Registries.BLOCK).orElseThrow();
        ((DefrostedRegistry) dimensions).setFrozen(false);
        ((DefrostedRegistry) biomeRegistry).setFrozen(false);

        for (Map.Entry<ResourceLocation, SkyDimensionData> entry : SkyDimensions.getSkyDimensions().entrySet()) {
            setupDimension(entry.getKey(), entry.getValue(), dimensions, blocks, biomeRegistry, dimensionTypeRegistry);
        }
        configureDimensionSynching(dimensions);

        ((DefrostedRegistry) dimensions).setFrozen(true);
        ((DefrostedRegistry) biomeRegistry).setFrozen(true);
    }

    private static void configureDimensionSynching(MappedRegistry<LevelStem> dimensions) {
        for (SkyDimensionData config : SkyDimensions.getSkyDimensions().values()) {
            if (!config.enabled) {
                continue;
            }

            LevelStem dimension = dimensions.get(new ResourceLocation(config.dimensionId));
            for (String synchDimId : config.synchToDimensions) {
                LevelStem synchDim =  dimensions.get(new ResourceLocation(synchDimId));
                if (DimensionType.getTeleportationScale(synchDim.type().value(), dimension.type().value()) > 1) {
                    ChunkByChunkConstants.LOGGER.warn("Cowardly refusing to synch dimension {} with {}, as the coordinate scale would result in a performance issues", config.dimensionId, synchDimId);
                    continue;
                }
                if (synchDim.generator() instanceof SkyChunkGenerator generator) {
                    generator.addSynchLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(config.dimensionId)));
                } else {
                    ChunkByChunkConstants.LOGGER.warn("Cannot synch dimension {} with {}, as it is not a sky dimension", config.dimensionId, synchDimId);
                }

            }
        }
    }

    private static void setupDimension(ResourceLocation skyDimensionId, SkyDimensionData config, MappedRegistry<LevelStem> dimensions, Registry<Block> blocks, WritableRegistry<Biome> biomeRegistry, Registry<DimensionType> dimensionTypeRegistry) {
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

        SkyChunkGenerator generator = setupCoreGenerationDimension(config, dimensions, blocks, biomeRegistry, level, rootGenerator);

        Holder<DimensionType> themeDimensionType = level.type();
        if (config.biomeThemeDimensionType != null && !config.biomeThemeDimensionType.isEmpty()) {
            Optional<Holder.Reference<DimensionType>> holder = dimensionTypeRegistry.getHolder(ResourceKey.create(Registries.DIMENSION_TYPE, new ResourceLocation(config.biomeThemeDimensionType)));
            if (holder.isPresent()) {
                themeDimensionType = holder.get();
            }
        }

        for (Map.Entry<String, List<String>> biomeTheme : config.biomeThemes.entrySet()) {
            ResourceKey<Level> biomeDim = setupThemeDimension(config.dimensionId, biomeTheme.getKey(), biomeTheme.getValue(), level, dimensions, rootGenerator, biomeRegistry, themeDimensionType);
            if (biomeDim != null) {
                generator.addBiomeDimension(biomeTheme.getKey(), biomeDim);
            }
        }
    }

    private static SkyChunkGenerator setupCoreGenerationDimension(SkyDimensionData config, MappedRegistry<LevelStem> dimensions, Registry<Block> blocks, Registry<Biome> biomes, LevelStem level, ChunkGenerator rootGenerator) {
        ResourceLocation genDimensionId = config.getGenDimensionId();
        ResourceKey<LevelStem> genLevelId = ResourceKey.create(Registries.LEVEL_STEM, genDimensionId);
        LevelStem generationLevel = dimensions.get(genDimensionId);
        if (generationLevel == null) {
            generationLevel = new LevelStem(level.type(), rootGenerator);
            dimensions.register(genLevelId, generationLevel, Lifecycle.stable());
        }

        SkyChunkGenerator skyGenerator;
        if (!(level.generator() instanceof SkyChunkGenerator)) {
            skyGenerator = new SkyChunkGenerator(rootGenerator);
            LevelStem newLevelStem = new LevelStem(level.type(), skyGenerator);
            int dimensionsId = dimensions.getId(level);
            dimensions.registerMapping(dimensionsId, ResourceKey.create(Registries.LEVEL_STEM, new ResourceLocation(config.dimensionId)), newLevelStem, Lifecycle.stable());
        } else {
            skyGenerator = (SkyChunkGenerator) level.generator();
        }
        Block sealBlock = blocks.get(new ResourceLocation(config.sealBlock));
        if (sealBlock == null) {
            sealBlock = Blocks.BEDROCK;
        }
        Block coverBlock = blocks.get(new ResourceLocation(config.sealCoverBlock));
        if (config.unspawnedBiome != null && !config.unspawnedBiome.isEmpty()) {
            biomes.getHolder(ResourceKey.create(Registries.BIOME, new ResourceLocation(config.unspawnedBiome))).ifPresent(skyGenerator::setUnspawnedBiome);
        }

        skyGenerator.configure(ResourceKey.create(Registries.DIMENSION, genLevelId.location()), config.generationType, sealBlock, coverBlock, config.initialChunks, config.allowChunkSpawner, config.allowUnstableChunkSpawner);
        return skyGenerator;
    }

    private static ResourceKey<Level> setupThemeDimension(String dimId, String themeName, List<String> biomes, LevelStem sourceLevel, MappedRegistry<LevelStem> dimensions, ChunkGenerator rootGenerator, WritableRegistry<Biome> biomeRegistry, Holder<DimensionType> themeDimensionType) {
        ResourceLocation biomeDimId = new ResourceLocation(dimId+ "_" + themeName + "_gen");
        List<ResourceKey<Biome>> biomeKeys = biomes.stream().map(x -> ResourceKey.create(Registries.BIOME, new ResourceLocation(x))).filter(key -> {
            boolean valid = biomeRegistry.containsKey(key);
            if (!valid) {
                ChunkByChunkConstants.LOGGER.warn("Could not resolve biome {} for {}", key, dimId);
            }
            return valid;
        }).toList();

        ResourceKey<LevelStem> levelKey = ResourceKey.create(Registries.LEVEL_STEM, biomeDimId);
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
                        builder.add(pair.mapSecond(biomeRegistry::getHolderOrThrow));
                    }
                });
                return new Climate.ParameterList<>(builder.build());
            }).biomeSource(biomeRegistry.createRegistrationLookup());
        }

        LevelStem biomeLevel = new LevelStem(themeDimensionType, new NoiseBasedChunkGenerator(source, ChunkGeneratorAccess.getNoiseGeneratorSettings(rootGenerator)));
        LevelStem existingMapping = dimensions.get(levelKey);
        if (existingMapping != null) {
            dimensions.registerMapping(dimensions.getId(existingMapping), levelKey, biomeLevel, Lifecycle.stable());
        } else {
            dimensions.register(levelKey, biomeLevel, Lifecycle.stable());
        }
        return ResourceKey.create(Registries.DIMENSION, biomeDimId);
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
                overworldSpawnPos = findAppropriateSpawnChunk(overworldLevel, generationLevel, server.registryAccess());
                spawnInitialChunks(overworldLevel, skyGenerator.getInitialChunks(), overworldSpawnPos, ChunkByChunkConfig.get().getGeneration().spawnNewChunkChest());
            }
        } else {
            overworldSpawnPos = overworldLevel.getSharedSpawnPos();
        }

        for (ServerLevel level : server.getAllLevels()) {
            if (level != overworldLevel && level.getChunkSource().getGenerator() instanceof SkyChunkGenerator levelGenerator) {
                if (levelGenerator.getInitialChunks() > 0) {
                    spawnInitialChunks(level, levelGenerator.getInitialChunks(), overworldSpawnPos, false);
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
     * @param registryAccess
     */
    private static BlockPos findAppropriateSpawnChunk(ServerLevel overworldLevel, ServerLevel generationLevel, RegistryAccess registryAccess) {
        if (ChunkByChunkConfig.get().getGeneration().isSpawnChunkStrip()) {
            return overworldLevel.getSharedSpawnPos();
        }

        TagKey<Block> logsTag = BlockTags.LOGS;
        TagKey<Block> leavesTag = BlockTags.LEAVES;
        Set<Block> copper = ImmutableSet.of(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK);

        BlockPos spawnPos = overworldLevel.getSharedSpawnPos();

        switch (ChunkByChunkConfig.get().getGameplayConfig().getStartRestriction()) {
            case Village -> {
                spawnPos = findVillage(generationLevel, registryAccess, spawnPos);
            }
            case Biome -> {
                String startingBiome = ChunkByChunkConfig.get().getGameplayConfig().getStartingBiome();
                spawnPos = findBiome(overworldLevel, registryAccess, spawnPos, startingBiome);
            }
        }

        ChunkPos initialChunkPos = new ChunkPos(spawnPos);
        SpiralIterator iterator = new SpiralIterator(initialChunkPos.x, initialChunkPos.z);
        int attempts = 0;
        while (attempts < MAX_FIND_CHUNK_ATTEMPTS) {
            LevelChunk chunk = generationLevel.getChunk(iterator.getX(), iterator.getY());
            if (ChunkUtil.countBlocks(chunk, logsTag) > 2
                    && ChunkUtil.countBlocks(chunk, Blocks.WATER) > 0
                    && ChunkUtil.countBlocks(chunk, leavesTag) > 3
                    && ChunkUtil.countBlocks(chunk, copper) >= 36) {
                spawnPos = new BlockPos(chunk.getPos().getMiddleBlockX(), ChunkUtil.getSafeSpawnHeight(chunk, chunk.getPos().getMiddleBlockX(), chunk.getPos().getMiddleBlockZ()), chunk.getPos().getMiddleBlockZ());
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
        ServerLevelData levelData = (ServerLevelData) overworldLevel.getLevelData();
        levelData.setSpawn(spawnPos, levelData.getSpawnAngle());
        return spawnPos;
    }

    private static BlockPos findBiome(ServerLevel overworldLevel, RegistryAccess registryAccess, BlockPos spawnPos, String startingBiome) {
        if (startingBiome.startsWith("#")) {
            Optional<HolderSet.Named<Biome>> tagSet = registryAccess.registry(Registries.BIOME).orElseThrow().getTag(TagKey.create(Registries.BIOME, new ResourceLocation(startingBiome.substring(1))));
            if (tagSet.isPresent()) {
                Pair<BlockPos, Holder<Biome>> location = overworldLevel.findClosestBiome3d(x -> tagSet.get().contains(x), spawnPos, 6400, 32, 64);
                if (location != null) {
                    spawnPos = location.getFirst();
                    ChunkByChunkConstants.LOGGER.info("Spawn shifted to nearest biome of tag " + startingBiome);
                }
            } else {
                ChunkByChunkConstants.LOGGER.warn("No biome matching '" + startingBiome + "' found");
            }
        } else {
            Biome biome = registryAccess.registry(Registries.BIOME).orElseThrow().get(new ResourceLocation(startingBiome));
            if (biome != null) {
                Pair<BlockPos, Holder<Biome>> location = overworldLevel.findClosestBiome3d(x -> x.value().equals(biome), spawnPos, 6400, 32, 64);
                if (location != null) {
                    spawnPos = location.getFirst();
                    ChunkByChunkConstants.LOGGER.info("Spawn shifted to nearest biome: " + startingBiome);
                } else {
                    ChunkByChunkConstants.LOGGER.warn("No biome matching '" + startingBiome + "' found");
                }
            }
        }
        return spawnPos;
    }

    private static BlockPos findVillage(ServerLevel generationLevel, RegistryAccess registryAccess, BlockPos spawnPos) {
        Registry<Structure> structures = registryAccess.registry(Registries.STRUCTURE).orElseThrow();
        Optional<HolderSet.Named<Structure>> structuresTag = structures.getTag(StructureTags.VILLAGE);
        if (structuresTag.isPresent()) {
            HolderSet<Structure> holders = structuresTag.get();
            Pair<BlockPos, Holder<Structure>> nearest = generationLevel.getChunkSource().getGenerator().findNearestMapStructure(generationLevel, holders, spawnPos, 100, false);
            if (nearest != null) {
                spawnPos = nearest.getFirst();
                ChunkByChunkConstants.LOGGER.info("Spawn shifted to nearest village");
            }
        } else {
            ChunkByChunkConstants.LOGGER.warn("Could not find village spawn");
        }
        return spawnPos;
    }

    /**
     * Spawns the initial chunks
     */
    private static void spawnInitialChunks(ServerLevel level, int initialChunks, BlockPos overworldSpawn, boolean spawnChest) {
        ChunkSpawnController chunkSpawnController = ChunkSpawnController.get(level.getServer());
        BlockPos scaledSpawn = new BlockPos(Mth.floor(overworldSpawn.getX() / level.dimensionType().coordinateScale()), overworldSpawn.getY(), Mth.floor(overworldSpawn.getZ() / level.dimensionType().coordinateScale()));
        ChunkPos centerChunkPos = new ChunkPos(scaledSpawn);
        if (initialChunks > 0 && initialChunks <= CHUNK_SPAWN_OFFSETS.size()) {
            List<int[]> chunkOffsets = CHUNK_SPAWN_OFFSETS.get(initialChunks - 1);
            for (int[] offset : chunkOffsets) {
                ChunkPos targetPos = new ChunkPos(centerChunkPos.x + offset[0], centerChunkPos.z + offset[1]);
                if (chunkSpawnController.request(level, "", false, targetPos.getMiddleBlockPosition(0), offset[0] == 0 && offset[1] == 0)) {
                    if (spawnChest && offset[0] == 0 && offset[1] == 0) {
                        SpawnChunkHelper.createNextSpawner(level, targetPos);
                    }
                }
            }
        } else {
            SpiralIterator spiralIterator = new SpiralIterator(centerChunkPos.x, centerChunkPos.z);
            for (int i = 0; i < initialChunks; i++) {
                ChunkPos targetPos = new ChunkPos(spiralIterator.getX(), spiralIterator.getY());
                if (chunkSpawnController.request(level, "", false, targetPos.getMiddleBlockPosition(0), i == 0)) {
                    if (spawnChest && i == 0) {
                        SpawnChunkHelper.createNextSpawner(level, targetPos);
                    }
                }
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

    public static void onLevelTick(MinecraftServer server) {
        ChunkSpawnController chunkSpawnController = ChunkSpawnController.get(server);
        if (chunkSpawnController != null) {
            chunkSpawnController.tick();
        }
    }
}
