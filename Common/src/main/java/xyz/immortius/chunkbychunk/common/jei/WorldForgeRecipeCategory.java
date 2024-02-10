package xyz.immortius.chunkbychunk.common.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.immortius.chunkbychunk.client.screens.WorldForgeScreen;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.ArrayList;
import java.util.List;

public class WorldForgeRecipeCategory implements IRecipeCategory<WorldForgeRecipe> {

    private final IDrawable background;
    private final IDrawable icon;
    private final List<IDrawableStatic> frames;
    private final ITickTimer frameTimer;

    public WorldForgeRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.createDrawable(WorldForgeScreen.CONTAINER_TEXTURE, 48,25, 94, 36);
        icon = guiHelper.createDrawableItemStack(new ItemStack(Services.PLATFORM.worldForgeBlockItem()));
        frames = new ArrayList<>();
        for (int i = 0; i < WorldForgeScreen.NUM_FRAMES; i++) {
            frames.add(guiHelper.createDrawable(WorldForgeScreen.CONTAINER_TEXTURE, 176, i * 11, 30, 11));
        }
        frameTimer = guiHelper.createTickTimer((int) WorldForgeScreen.TICKS_PER_FRAME * frames.size(), frames.size() - 1, false);
    }

    @Override
    public RecipeType<WorldForgeRecipe> getRecipeType() {
        return CBCJeiPlugin.WORLD_FORGE;
    }

    @Override
    public ResourceLocation getUid() {
        return getRecipeType().getUid();
    }

    @Override
    public Class<? extends WorldForgeRecipe> getRecipeClass() {
        return getRecipeType().getRecipeClass();
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("block.chunkbychunk.worldforge");
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
    public void draw(WorldForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        frames.get(frameTimer.getValue()).draw(stack, 30, 12, 0, 0, 0, Math.max(0, 30 - 30 * recipe.getFuelValue() / ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorldForgeRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10).setSlotName("Input").addIngredients(VanillaTypes.ITEM_STACK, recipe.getInputItems());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 10).setSlotName("Output").addIngredient(VanillaTypes.ITEM_STACK, recipe.getOutput());
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStack(Services.PLATFORM.worldForgeBlockItem().getDefaultInstance());
    }
}
