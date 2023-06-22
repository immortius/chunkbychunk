package xyz.immortius.chunkbychunk.client.screens;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldScannerBlockEntity;
import xyz.immortius.chunkbychunk.common.menus.WorldScannerMenu;

/**
 * Screen for the WorldScanner. Renders the scanned map (if any)
 */
public class WorldScannerScreen extends AbstractContainerScreen<WorldScannerMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation(ChunkByChunkConstants.MOD_ID + ":textures/gui/container/worldscanner.png");

    private static final int MAIN_TEXTURE_DIM = 512;
    private static final int MAP_DIMENSIONS = 128;
    private static final float TICKS_PER_FRAME = 4f;
    private static final int NUM_FRAMES = 8;

    private float animCounter = 0.f;
    private MapRenderer mapRenderer;

    public WorldScannerScreen(WorldScannerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        imageWidth = 310;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        mapRenderer = minecraft.gameRenderer.getMapRenderer();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int cursorX, int cursorY) {
        super.renderTooltip(graphics, cursorX, cursorY);
        int mapX = cursorX - 174 - leftPos;
        int mapY = cursorY - 18 - topPos;
        if (mapX >= 0 && mapY >= 0 && mapX < MAP_DIMENSIONS && mapY < MAP_DIMENSIONS) {
            mapX = mapX / WorldScannerBlockEntity.SCAN_ZOOM - WorldScannerBlockEntity.SCAN_CENTER;
            mapY = mapY / WorldScannerBlockEntity.SCAN_ZOOM - WorldScannerBlockEntity.SCAN_CENTER;
            StringBuilder builder = new StringBuilder();
            if (mapY < 0) {
                builder.append(-mapY);
                builder.append(" N ");
            } else if (mapY > 0) {
                builder.append(mapY);
                builder.append(" S ");
            }
            if (mapX < 0) {
                builder.append(-mapX);
                builder.append(" W");
            } else if (mapX > 0) {
                builder.append(mapX);
                builder.append(" E");
            }

            if (builder.length() > 0) {
                graphics.renderTooltip(this.font, Component.literal(builder.toString()), cursorX, cursorY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        animCounter += delta;
        while (animCounter > TICKS_PER_FRAME * NUM_FRAMES) {
            animCounter -= TICKS_PER_FRAME * NUM_FRAMES;
        }
        int frame = Mth.floor(animCounter / TICKS_PER_FRAME);

        graphics.blit(CONTAINER_TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, MAIN_TEXTURE_DIM, MAIN_TEXTURE_DIM);
        if (menu.getEnergy() > 0) {
            int display = Mth.ceil(7.f * menu.getEnergy() / menu.getMaxEnergy());
            graphics.blit(CONTAINER_TEXTURE, leftPos + 54, topPos + 56, 128 + 12 * display, 166 + 12 * frame, 13, 13, MAIN_TEXTURE_DIM, MAIN_TEXTURE_DIM);
        }
        if (menu.isMapAvailable()) {
            renderMap(graphics);
        }
        graphics.blit(CONTAINER_TEXTURE, leftPos + 234, topPos + 78, 124, 166 + frame * 4, 4, 4, MAIN_TEXTURE_DIM, MAIN_TEXTURE_DIM);

    }

    private void renderMap(GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 174, topPos + 18, 1.0D);
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        MapItemSavedData mapData = this.minecraft.level.getMapData(menu.getMapKey());
        if (mapData != null) {
            mapRenderer.render(graphics.pose(), buffer, menu.getMapId(), mapData, true, 0xFFFFFF);
        }
        buffer.endBatch();
        graphics.pose().popPose();
    }


}
