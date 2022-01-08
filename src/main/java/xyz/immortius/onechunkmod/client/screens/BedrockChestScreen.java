package xyz.immortius.onechunkmod.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.immortius.onechunkmod.client.menus.BedrockChestMenu;

public class BedrockChestScreen extends AbstractContainerScreen<BedrockChestMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation("onechunkmod:textures/gui/container/bedrockchest.png");

    public BedrockChestScreen(BedrockChestMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        ++this.imageHeight;
    }

    public void render(PoseStack p_99249_, int p_99250_, int p_99251_, float p_99252_) {
        this.renderBackground(p_99249_);
        super.render(p_99249_, p_99250_, p_99251_, p_99252_);
        this.renderTooltip(p_99249_, p_99250_, p_99251_);
    }

    protected void renderBg(PoseStack p_99244_, float p_99245_, int p_99246_, int p_99247_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CONTAINER_TEXTURE);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(p_99244_, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }
}
