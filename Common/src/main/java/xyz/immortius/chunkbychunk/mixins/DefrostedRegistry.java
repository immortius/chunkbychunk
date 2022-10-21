package xyz.immortius.chunkbychunk.mixins;

import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MappedRegistry.class)
public interface DefrostedRegistry {
    @Accessor("frozen")
    void setFrozen(boolean newFrozen);
}