package xyz.immortius.chunkbychunk.common.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.util.SpiralIterator;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.Collections;

public class WorldScannerRecipeCategory  implements IRecipeCategory<WorldScannerRecipe> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ChunkByChunkConstants.MOD_ID + ":textures/gui/container/worldscannerjei.png");

    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawableStatic scanSquare;

    public WorldScannerRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(Services.PLATFORM.worldScannerBlockItem()));
        background = guiHelper.drawableBuilder(BACKGROUND_TEXTURE, 0, 0, 119, 78).setTextureSize(256, 256).build();
        scanSquare = guiHelper.drawableBuilder(BACKGROUND_TEXTURE, 0, 78, 4, 4).setTextureSize(256, 256).build();
    }

    @Override
    public RecipeType<WorldScannerRecipe> getRecipeType() {
        return CBCJeiPlugin.WORLD_SCANNER;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.chunkbychunk.worldscanner");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(WorldScannerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        int partValue = recipe.getValue() * 4 / ChunkByChunkConfig.get().getWorldScannerConfig().getFuelRequiredPerChunk();
        int squares = Math.min(partValue / 4, 361);
        int remainder = partValue % 4;

        SpiralIterator iterator = new SpiralIterator(0,0);
        for (int i = 0; i < squares; i++) {
           scanSquare.draw(graphics, 78 + iterator.getX() * 4, 37 + iterator.getY() * 4);
           iterator.next();
        }
        if (remainder > 0 && squares < 361) {
            scanSquare.draw(graphics, 78 + iterator.getX() * 4, 37 + iterator.getY() * 4, 0, 2, 0, (remainder > 1) ? 0 : 2);
            if (remainder == 3) {
                scanSquare.draw(graphics, 78 + iterator.getX() * 4, 37 + iterator.getY() * 4, 2, 0, 0, 2);
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorldScannerRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 14, 28).setSlotName("Input").addIngredients(VanillaTypes.ITEM_STACK, Collections.singletonList(recipe.getItem()));
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStack(Services.PLATFORM.worldScannerBlockItem().getDefaultInstance());
    }
}
