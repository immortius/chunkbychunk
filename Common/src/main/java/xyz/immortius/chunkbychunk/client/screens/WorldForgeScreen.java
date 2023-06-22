package xyz.immortius.chunkbychunk.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.menus.WorldForgeMenu;

/**
 * Screen for the World Forge - this is a single input/output style furnace with animated progress arrow
 */
public class WorldForgeScreen extends AbstractContainerScreen<WorldForgeMenu> {
    public static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation(ChunkByChunkConstants.MOD_ID + ":textures/gui/container/worldforge.png");

    public static final float TICKS_PER_FRAME = 2f;
    public static final int NUM_FRAMES = 8;

    private float animCounter = 0.f;

    public WorldForgeScreen(WorldForgeMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        animCounter += delta;
        while (animCounter > TICKS_PER_FRAME * NUM_FRAMES) {
            animCounter -= TICKS_PER_FRAME * NUM_FRAMES;
        }
        int frame = Mth.floor(animCounter / TICKS_PER_FRAME);

        graphics.blit(CONTAINER_TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (menu.getProgress() > 0)
        {
            int completion = 0;
            int goal = menu.getGoal();
            if (goal > 0) {
                int progress = Math.min(goal, menu.getProgress());
                completion = 30 * progress / goal;
            }
            graphics.blit(CONTAINER_TEXTURE, leftPos + 78, topPos + 37, 176, frame * 11, completion, 11);
        }
    }
}
