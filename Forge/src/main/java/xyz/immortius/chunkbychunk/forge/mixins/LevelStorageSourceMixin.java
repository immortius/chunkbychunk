package xyz.immortius.chunkbychunk.forge.mixins;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;
import java.util.Set;

@Mixin(LevelStorageSource.class)
public class LevelStorageSourceMixin {

    @ModifyVariable(method = "readWorldGenSettings", argsOnly = true, at = @At("HEAD"))
    private static Dynamic<Tag> fixDynamicData(Dynamic<Tag> dynamicData) {
        Set<String> presets = ImmutableSet.of("nether", "overworld");

        Dynamic<Tag> worldGenSettings = dynamicData.get("WorldGenSettings").orElseEmptyMap();
        Dynamic<Tag> dimensions = worldGenSettings.get("dimensions").orElseEmptyMap();
        for (String dimId : ((CompoundTag) dimensions.getValue()).getAllKeys()) {
            Dynamic<Tag> dimension = dimensions.get(dimId).orElseEmptyMap();
            Dynamic<Tag> generator = dimension.get("generator").orElseEmptyMap();
            Dynamic<Tag> biomeSource = generator.get("biome_source").orElseEmptyMap();
            Optional<String> presetTag = biomeSource.get("preset").asString().result();
            if (presetTag.isPresent()) {
                String preset = presetTag.get();
                if (!presets.contains(preset)) {
                    dimensions = dimensions.remove(dimId);
                }
            }
        }
        worldGenSettings = worldGenSettings.set("dimensions", dimensions);
        return dynamicData.set("WorldGenSettings", worldGenSettings);
    }
}
