package xyz.immortius.chunkbychunk.common.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.common.blockEntities.WorldForgeBlockEntity;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.interop.Services;

import java.util.Collections;

@JeiPlugin
public class CBCJeiPlugin implements IModPlugin {
    public static final RecipeType<WorldForgeRecipe> WORLD_FORGE =
            RecipeType.create(ChunkByChunkConstants.MOD_ID, "worldforge", WorldForgeRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ChunkByChunkConstants.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WorldForgeRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(WORLD_FORGE, WorldForgeBlockEntity.FUEL_TAGS.entrySet().stream().map(tagInfo -> {
            ItemStack output = determineOutput(tagInfo.getValue().get());
            return new WorldForgeRecipe(registration.getJeiHelpers().getIngredientManager().getAllItemStacks().stream().filter(item -> item.is(tagInfo.getKey())).toList(), tagInfo.getValue().get(), output);
        }).filter(r -> !r.getInputItems().isEmpty()).toList());
        registration.addRecipes(WORLD_FORGE, WorldForgeBlockEntity.FUEL.entrySet().stream().map(fuelInfo -> {
            ItemStack output = determineOutput(fuelInfo.getValue().get());
            return new WorldForgeRecipe(Collections.singletonList(fuelInfo.getKey().getDefaultInstance()), fuelInfo.getValue().get(), output);
        }).toList());
        registration.addRecipes(WORLD_FORGE, WorldForgeBlockEntity.CRYSTAL_STEPS.entrySet().stream().map(step -> {
            ItemStack input = step.getKey().getDefaultInstance().copyWithCount(WorldForgeBlockEntity.GROW_CRYSTAL_AT);
            ItemStack output = step.getValue().getDefaultInstance();
            return new WorldForgeRecipe(Collections.singletonList(input), ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost(), output);
        }).toList());
    }

    @NotNull
    private ItemStack determineOutput(int fuelValue) {
        int count = fuelValue / ChunkByChunkConfig.get().getWorldForge().getFragmentFuelCost();
        ItemStack output = Services.PLATFORM.worldFragmentItem().getDefaultInstance();
        if (count > 1) {
            output = output.copyWithCount(count);
        }
        return output;
    }
}
