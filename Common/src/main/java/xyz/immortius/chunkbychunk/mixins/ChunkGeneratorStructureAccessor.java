package xyz.immortius.chunkbychunk.mixins;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

/**
 * Mixin to support accessing protected fields of ChunkGenerator for wrapping
 */
@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorStructureAccessor {

    @Accessor("structureSets")
    Registry<StructureSet> getStructureSet();

    @Accessor("structureOverrides")
    Optional<HolderSet<StructureSet>> getStructureOverrides();
}