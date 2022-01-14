package xyz.immortius.chunkbychunk.interop;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Methods whose implementation varies by mod system
 */
public final class CBCInteropMethods {
    private CBCInteropMethods() {
    }

    @Nullable
    public static Entity changeDimension(Entity entity, ServerLevel level, PortalInfo portalInfo) {
        return entity.changeDimension(level, new EntityTeleport(portalInfo));
    }

    public static void loadServerConfig(MinecraftServer server) {
    }

    private static class EntityTeleport implements ITeleporter {

        private final PortalInfo portalInfo;

        public EntityTeleport(PortalInfo portalInfo) {
            this.portalInfo = portalInfo;
        }

        @Override
        public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
            return portalInfo;
        }

        @Override
        public boolean isVanilla() {
            return false;
        }

        @Override
        public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
            return false;
        }
    }

    ;
}
