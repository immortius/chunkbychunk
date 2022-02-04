package xyz.immortius.chunkbychunk.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.MaterialColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

public class WorldScannerScreen extends AbstractContainerScreen<WorldScannerMenu> {
    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);
    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation(ChunkByChunkConstants.MOD_ID + ":textures/gui/container/worldscanner.png");

    private static final int MAIN_TEXTURE_DIM = 512;
    private static final int MAP_DIMENSIONS = 149;
    private static final int MAP_TEX_DIMENSIONS = 256;
    private static final float TICKS_PER_FRAME = 4f;
    private static final int NUM_FRAMES = 8;

    private float animCounter = 0.f;

    private ResourceLocation mapLocation;
    private DynamicTexture mapTexture;
    private boolean scanChanged = true;

    public WorldScannerScreen(WorldScannerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        imageWidth = 331;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        LOGGER.info("Initialising scanner screen");
        this.mapTexture = new DynamicTexture(MAP_TEX_DIMENSIONS, MAP_TEX_DIMENSIONS, true);
        mapLocation = minecraft.textureManager.register("chunkscanmap", mapTexture);
    }

    @Override
    public void onClose() {
        super.onClose();
        LOGGER.info("Closing scanner screen");
        mapTexture.close();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (scanChanged) {
            byte[] map = menu.getScannerEntity().getMap();
            if (map.length == MAP_DIMENSIONS * MAP_DIMENSIONS) {
                for (int i = 0; i < MAP_DIMENSIONS; ++i) {
                    for (int j = 0; j < MAP_DIMENSIONS; ++j) {
                        int k = j + i * MAP_DIMENSIONS;
                        this.mapTexture.getPixels().setPixelRGBA(j, i, MaterialColor.getColorFromPackedId(map[k]));
                    }
                }
                mapTexture.upload();
            }
        }
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack stack, float delta, int mouseX, int mouseY) {
        animCounter += delta;
        while (animCounter > TICKS_PER_FRAME * NUM_FRAMES) {
            animCounter -= TICKS_PER_FRAME * NUM_FRAMES;
        }
        int frame = Mth.floor(animCounter / TICKS_PER_FRAME);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CONTAINER_TEXTURE);
        int i = leftPos;
        int j = topPos;
        this.blit(stack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        if (menu.getEnergy() > 0) {
            int display = 100 * menu.getEnergy() / menu.getMaxEnergy();
            this.blit(stack, i + 58, j + 55, 24, 172 + frame * 6, display, 6);
        }
        RenderSystem.setShaderTexture(0, mapLocation);
        this.blit(stack, i + 174, j + 9, 0, 0, MAP_TEX_DIMENSIONS, MAP_TEX_DIMENSIONS, MAP_TEX_DIMENSIONS);
        RenderSystem.setShaderTexture(0, CONTAINER_TEXTURE);
        this.blit(stack, i + 174 + 74, j + 9 + 74, 24, 172 + frame * 6, 1, 1);
    }

    @Override
    public void blit(PoseStack stack, int screenX, int screenY, int texX, int texY, int pixelWidth, int pixelHeight) {
        blit(stack, screenX, screenY, getBlitOffset(), (float) texX, (float) texY, pixelWidth, pixelHeight, MAIN_TEXTURE_DIM, MAIN_TEXTURE_DIM);
    }

    public void blit(PoseStack stack, int screenX, int screenY, int texX, int texY, int pixelWidth, int pixelHeight, int textureDim) {
        blit(stack, screenX, screenY, getBlitOffset(), (float) texX, (float) texY, pixelWidth, pixelHeight, textureDim, textureDim);
    }


}
