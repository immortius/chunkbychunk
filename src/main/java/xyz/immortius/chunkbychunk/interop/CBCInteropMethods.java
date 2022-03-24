package xyz.immortius.chunkbychunk.interop;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.util.ITeleporter;

import java.util.Optional;
import java.util.function.Function;

/**
 * Static methods whose implementation varies by mod system
 */
public final class CBCInteropMethods {
    private CBCInteropMethods() {
    }

    /**
     * Change the dimension of an entity.
     * For forge, this uses the forge extension changeDimension method
     *
     * @param entity     The entity to move
     * @param level      The level to move the entity to
     * @param portalInfo Portal information for the move
     * @return The moved entity
     */
    public static Entity changeDimension(Entity entity, ServerLevel level, PortalInfo portalInfo) {
        return entity.changeDimension(level, new EntityTeleport(portalInfo));
    }

    /**
     * @param bucket The bucket to get the contents of
     * @return The fluid contents of the bucket (may be null)
     */
    public static Fluid getBucketContents(BucketItem bucket) {
        return bucket.getFluid();
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

    /**
     * @param generator
     * @return The structure sets for a given generator
     */
    public static Registry<StructureSet> getStructureSets(ChunkGenerator generator) {
        return generator.structureSets;
    }

    /**
     * @param generator
     * @return The structure overrides for a given generator
     */
    public static Optional<HolderSet<StructureSet>> getStructureOverrides(ChunkGenerator generator) {
        return generator.structureOverrides;
    }
}
