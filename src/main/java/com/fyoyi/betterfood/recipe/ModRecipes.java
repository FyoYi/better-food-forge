package com.fyoyi.betterfood.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    // 这个DeferredRegister是用来注册 RecipeSerializer 的
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "better_food");

    public static final RegistryObject<RecipeSerializer<FireworkArrowRecipe>> FIREWORK_ARROW =
            RECIPE_SERIALIZERS.register("firework_arrow", () ->
                    new SimpleCraftingRecipeSerializer<>(FireworkArrowRecipe::new));

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
