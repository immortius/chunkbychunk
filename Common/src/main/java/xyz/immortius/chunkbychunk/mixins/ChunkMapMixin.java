package xyz.immortius.chunkbychunk.mixins;

import com.mojang.datafixers.DataFixer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.world.ControllableChunkMap;
import xyz.immortius.chunkbychunk.common.world.SpawnChunkHelper;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.Services;

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
    protected void updateChunkTracking(ServerPlayer p_183755_, ChunkPos p_183756_, MutableObject<ClientboundLevelChunkWithLightPacket> p_183757_, boolean p_183758_, boolean p_183759_) {
    }

    public void forceReloadChunk(ChunkPos chunk) {
        ChunkMap thisMap = (ChunkMap) (Object) this;
        for (ServerPlayer player : thisMap.getPlayers(chunk, false)) {
            updateChunkTracking(player, chunk, new MutableObject<>(), false, true);
        }
    }

    @Inject(method = "onFullChunkStatusChange", at = @At("HEAD"))
    public void onFullStatusChange(ChunkPos pos, ChunkHolder.FullChunkStatus status, CallbackInfo ci) {
        if (ChunkByChunkConfig.get().getGeneration().isSpawnChunkStrip() && status == ChunkHolder.FullChunkStatus.ENTITY_TICKING && level.dimension().equals(Level.OVERWORLD) && new ChunkPos(level.getSharedSpawnPos()).x == pos.x && SpawnChunkHelper.isEmptyChunk(level, pos)) {
            BlockPos blockPos = pos.getMiddleBlockPosition(level.getMaxBuildHeight() - 1);
            level.setBlock(blockPos, Services.PLATFORM.triggeredSpawnChunkBlock().defaultBlockState(), Block.UPDATE_NONE);
        }
    }
}
