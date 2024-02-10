package xyz.immortius.chunkbychunk.mixins;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseBasedChunkGenerator.class)
public interface NoiseBasedChunkGeneratorMixin {
    @Accessor("noises")
    Registry<NormalNoise.NoiseParameters> getNoises();

    @Accessor("settings")
    Holder<NoiseGeneratorSettings> getSettings();

    @Accessor("seed")
    long getSeed();
}