package xyz.immortius.chunkbychunk.mixins;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseBasedChunkGenerator.class)
public interface NoiseBasedChunkGeneratorMixin {
    @Accessor("noises")
    Registry<NormalNoise.NoiseParameters> getNoises();
}