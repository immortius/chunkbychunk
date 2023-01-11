package xyz.immortius.chunkbychunk.common.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.client.screens.WorldMenderScreen;
import xyz.immortius.chunkbychunk.interop.Services;

public class WorldMenderRecipeCategory implements IRecipeCategory<WorldMenderRecipe> {

    private final IDrawable icon;
    private final IDrawable background;

    public WorldMenderRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(Services.PLATFORM.worldMenderBlockItem()));
        background = guiHelper.drawableBuilder(WorldMenderScreen.CONTAINER_TEXTURE, 64, 53, 48, 48).setTextureSize(512, 512).build();
    }

    @Override
    public RecipeType<WorldMenderRecipe> getRecipeType() {
        return CBCJeiPlugin.WORLD_MENDER;
    }

    @Override
    public ResourceLocation getUid() {
        return getRecipeType().getUid();
    }

    @Override
    public Class<? extends WorldMenderRecipe> getRecipeClass() {
        return getRecipeType().getRecipeClass();
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("block.chunkbychunk.worldmender");
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
    public void setRecipe(IRecipeLayoutBuilder builder, WorldMenderRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 16, 16).addItemStack(recipe.getInput());
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStack(Services.PLATFORM.worldMenderBlockItem().getDefaultInstance());
    }
}
