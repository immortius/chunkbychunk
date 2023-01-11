package xyz.immortius.chunkbychunk.mixins;

import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;


@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorAccessor {

    @Invoker
    List<StructurePlacement> callGetPlacementsForFeature(Holder<ConfiguredStructureFeature<?,?>> structure);

}