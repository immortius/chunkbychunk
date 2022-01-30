package xyz.immortius.chunkbychunk.server;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import xyz.immortius.chunkbychunk.common.world.SkyChunkGenerator;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.interop.CBCInteropMethods;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkSettings;

import java.util.List;
import java.util.OptionalInt;


public final class ServerEventHandler {

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

    public static void onServerStarting(MinecraftServer server) {
        WorldGenSettings worldGenSettings = server.getWorldData().worldGenSettings();
        LevelStem overworldStem = worldGenSettings.dimensions().get(Level.OVERWORLD.location());
        LevelStem generationStem = worldGenSettings.dimensions().get(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL.location());
        if (!(overworldStem.generator() instanceof SkyChunkGenerator)) {
            Registry<DimensionType> dimensionTypeRegistry = server.registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
            if (generationStem == null) {
                ResourceKey<LevelStem> skychunkgeneration = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(ChunkByChunkConstants.MOD_ID, "skychunkgeneration"));
                generationStem = new LevelStem(() -> dimensionTypeRegistry.get(DimensionType.OVERWORLD_LOCATION), overworldStem.generator());
                worldGenSettings.dimensions().register(skychunkgeneration, generationStem, Lifecycle.stable());
            }

            LevelStem newOverworldStem = new LevelStem(() -> dimensionTypeRegistry.get(DimensionType.OVERWORLD_LOCATION), new SkyChunkGenerator(overworldStem.generator(), ChunkByChunkSettings.sealWorld()));
            worldGenSettings.dimensions().registerOrOverride(OptionalInt.empty(), ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, Level.OVERWORLD.location()), newOverworldStem, Lifecycle.stable());
        }

    }

    public static void onServerStarted(MinecraftServer server) {
        CBCInteropMethods.loadServerConfig(server);
        checkSpawnInitialChunks(server);
    }

    private static void checkSpawnInitialChunks(MinecraftServer server) {
        ServerLevel overworldLevel = server.getLevel(Level.OVERWORLD);
        ServerLevel generationLevel = server.getLevel(ChunkByChunkConstants.SKY_CHUNK_GENERATION_LEVEL);

        if (overworldLevel != null && generationLevel != null) {
            BlockPos spawnPos = generationLevel.getSharedSpawnPos();
            ChunkPos chunkSpawnPos = new ChunkPos(spawnPos);
            if (SpawnChunkHelper.isEmptyChunk(overworldLevel, chunkSpawnPos)) {
                spawnInitialChunks(overworldLevel, chunkSpawnPos);
            }
        }
    }

    private static void spawnInitialChunks(ServerLevel overworldLevel, ChunkPos centerChunkPos) {
        List<int[]> chunkOffsets = CHUNK_SPAWN_OFFSETS.get(ChunkByChunkSettings.initialChunks() - 1);
        for (int[] offset : chunkOffsets) {
            ChunkPos targetPos = new ChunkPos(centerChunkPos.x + offset[0], centerChunkPos.z + offset[1]);
            ChunkPos sourcePos = new ChunkPos(targetPos.x + ChunkByChunkSettings.chunkGenXOffset(), targetPos.z + ChunkByChunkSettings.chunkGenZOffset());
            SpawnChunkHelper.spawnChunk(overworldLevel, sourcePos, targetPos);
        }
    }
}
