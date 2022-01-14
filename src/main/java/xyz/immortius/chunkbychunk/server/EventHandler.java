package xyz.immortius.chunkbychunk.server;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.interop.CBCInteropMethods;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkSettings;

import java.util.List;


public final class EventHandler {

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

    private EventHandler() {

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
