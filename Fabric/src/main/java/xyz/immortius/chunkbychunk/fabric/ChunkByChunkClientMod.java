package xyz.immortius.chunkbychunk.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.client.screens.BedrockChestScreen;
import xyz.immortius.chunkbychunk.client.screens.WorldForgeScreen;
import xyz.immortius.chunkbychunk.client.screens.WorldScannerScreen;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

/**
 * Client-only mod initialization
 */
public class ChunkByChunkClientMod implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Client Initializing");
        MenuScreens.register(ChunkByChunkMod.BEDROCK_CHEST_MENU, BedrockChestScreen::new);
        MenuScreens.register(ChunkByChunkMod.WORLD_FORGE_MENU, WorldForgeScreen::new);
        MenuScreens.register(ChunkByChunkMod.WORLD_SCANNER_MENU, WorldScannerScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(ChunkByChunkMod.CONFIG_PACKET, (client, handler, buf, responseSender) -> {
            LOGGER.info("Receiving config from server");
            ChunkByChunkConfig.get().getGameplayConfig().setBlockPlacementAllowedOutsideSpawnedChunks(buf.readBoolean());
        });
    }

}
