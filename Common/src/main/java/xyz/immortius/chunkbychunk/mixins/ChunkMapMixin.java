package xyz.immortius.chunkbychunk.mixins;

import com.mojang.datafixers.DataFixer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.server.world.ChunkSpawnController;
import xyz.immortius.chunkbychunk.server.world.ControllableChunkMap;

import java.nio.file.Path;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin extends ChunkStorage implements ChunkHolder.PlayerProvider, ControllableChunkMap {
    public ChunkMapMixin(Path $$0, DataFixer $$1, boolean $$2) {
        super($$0, $$1, $$2);
    }

    @Final
    @Shadow
    ServerLevel level;

    @Shadow
    private void markChunkPendingToSend(ServerPlayer player, ChunkPos chunkPos) {
    }

    public void forceReloadChunk(ChunkPos chunk) {
        ChunkMap thisMap = (ChunkMap) (Object) this;
        for (ServerPlayer player : thisMap.getPlayers(chunk, false)) {
            markChunkPendingToSend(player, chunk);
        }
    }

    @Inject(method = "onFullChunkStatusChange", at = @At("HEAD"))
    public void onFullStatusChange(ChunkPos pos, FullChunkStatus status, CallbackInfo ci) {
        if (ChunkByChunkConfig.get().getGeneration().isSpawnChunkStrip() && status.isOrAfter(FullChunkStatus.ENTITY_TICKING) && level.dimension().equals(Level.OVERWORLD) && new ChunkPos(level.getSharedSpawnPos()).x == pos.x) {
            BlockPos blockPos = pos.getMiddleBlockPosition(level.getMaxBuildHeight() - 1);
            ChunkSpawnController.get(level.getServer()).request(level, "", false, blockPos);
        }
    }
}
