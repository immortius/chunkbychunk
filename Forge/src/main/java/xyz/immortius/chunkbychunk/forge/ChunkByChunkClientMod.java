package xyz.immortius.chunkbychunk.forge;

import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;
import xyz.immortius.chunkbychunk.client.screens.ChunkByChunkConfigScreen;

public final class ChunkByChunkClientMod {

    private ChunkByChunkClientMod() {
    }

    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ChunkByChunkConfigScreen(screen)));
    }
}
