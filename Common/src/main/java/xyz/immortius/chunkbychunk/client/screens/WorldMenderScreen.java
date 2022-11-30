package xyz.immortius.chunkbychunk.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.menus.WorldMenderMenu;

/**
 * Screen for the WorldMender.
 */
public class WorldMenderScreen extends AbstractContainerScreen<WorldMenderMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation(ChunkByChunkConstants.MOD_ID + ":textures/gui/container/worldmender.png");

    private static final int MAIN_TEXTURE_DIM = 512;
    private static final float TICKS_PER_FRAME = 4f;
    private static final int NUM_FRAMES = 8;

    private static final int[][] NODE_OFFSETS = new int[][] {{3},{5,3},{6,5,3},{7,6,5,3},{7,7,7,6,4},{7,8,8,8,7,4},{7,8,9,9,9,7,5}};

    private float animCounter = 0.f;

    public WorldMenderScreen(WorldMenderMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        imageWidth = 176;
        imageHeight = 235;
        titleLabelX = 8;
        titleLabelY = 4;
        inventoryLabelX = 8;
        inventoryLabelY = 143;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        this.font.draw(stack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(stack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }

    @Override
    protected void renderTooltip(PoseStack stack, int cursorX, int cursorY) {
        super.renderTooltip(stack, cursorX, cursorY);
    }

    @Override
    protected void renderBg(PoseStack stack, float delta, int mouseX, int mouseY) {
        animCounter += delta;
        while (animCounter > TICKS_PER_FRAME * NUM_FRAMES) {
            animCounter -= TICKS_PER_FRAME * NUM_FRAMES;
        }

        int frame = Mth.floor(animCounter / TICKS_PER_FRAME);


        int highlightOffsetX = 176 + (frame / 4) * 128;
        int highlightOffsetY = 128 * (frame % 4);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CONTAINER_TEXTURE);
        this.blit(stack, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.blit(stack, leftPos + 24, topPos + 13, highlightOffsetX, highlightOffsetY, 128, 128);

        Integer nextChunk = menu.nextChunk();
        if (nextChunk != null) {
            Pos renderPos = getChunkPos(nextChunk);
            this.blit(stack, leftPos + renderPos.x, topPos + renderPos.y, 0, this.imageHeight, 2, 2);
        }
    }

    private Pos getChunkPos(int chunk) {
        int nextChunkX = chunk % 17 - 8;
        int nextChunkY = chunk / 17 - 8;

        int absX = Mth.abs(nextChunkX);
        int absY = Mth.abs(nextChunkY);
        int sigX = Mth.sign(nextChunkX);
        int sigY = Mth.sign(nextChunkY);

        int xOffset = nextChunkX * 7 + 6 * sigX + 63;
        int yOffset = nextChunkY * 7 + 6 * sigY + 63;

        if (absX > 0 && absY > absX) {
            xOffset -= sigX * NODE_OFFSETS[absY - 2][absX - 1];
        } else if (absY > 0 && absX > absY) {
            yOffset -= sigY * NODE_OFFSETS[absX - 2][absY - 1];
        }

        return new Pos(24 + xOffset, 13 + yOffset);
    }

    @Override
    public void blit(PoseStack stack, int screenX, int screenY, int texX, int texY, int pixelWidth, int pixelHeight) {
        blit(stack, screenX, screenY, getBlitOffset(), (float) texX, (float) texY, pixelWidth, pixelHeight, MAIN_TEXTURE_DIM, MAIN_TEXTURE_DIM);
    }

    private record Pos(int x, int y) {
    }

}
