package xyz.immortius.chunkbychunk.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.immortius.chunkbychunk.common.menus.BedrockChestMenu;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

/**
 * Screen for the Bedrock Chest
 */
public class BedrockChestScreen extends AbstractContainerScreen<BedrockChestMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation(ChunkByChunkConstants.MOD_ID + ":textures/gui/container/bedrockchest.png");

    public BedrockChestScreen(BedrockChestMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        ++this.imageHeight;
    }

    @Override
    public void render(PoseStack stack, int p_99250_, int p_99251_, float p_99252_) {
        this.renderBackground(stack);
        super.render(stack, p_99250_, p_99251_, p_99252_);
        this.renderTooltip(stack, p_99250_, p_99251_);
    }

    @Override
    protected void renderBg(PoseStack stack, float p_99245_, int p_99246_, int p_99247_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CONTAINER_TEXTURE);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(stack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }
}
