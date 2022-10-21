package xyz.immortius.chunkbychunk.mixins;

import com.mojang.datafixers.DataFixer;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.immortius.chunkbychunk.common.world.ControllableChunkMap;

import java.nio.file.Path;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin extends ChunkStorage implements ChunkHolder.PlayerProvider, ControllableChunkMap {
    public ChunkMapMixin(Path $$0, DataFixer $$1, boolean $$2) {
        super($$0, $$1, $$2);
    }

    @Shadow
    protected void updateChunkTracking(ServerPlayer p_183755_, ChunkPos p_183756_, MutableObject<ClientboundLevelChunkWithLightPacket> p_183757_, boolean p_183758_, boolean p_183759_) {
    }

    public void forceReloadChunk(ChunkPos chunk) {
        ChunkMap thisMap = (ChunkMap) (Object) this;
        for (ServerPlayer player : thisMap.getPlayers(chunk, false)) {
            updateChunkTracking(player, chunk, new MutableObject<>(), false, true);
        }
    }
}
