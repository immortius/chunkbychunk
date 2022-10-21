package xyz.immortius.chunkbychunk.forge;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import xyz.immortius.chunkbychunk.client.screens.ChunkByChunkConfigScreen;

public final class ChunkByChunkClientMod {

    private ChunkByChunkClientMod() {
    }

    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ChunkByChunkConfigScreen(screen)));
    }
}
