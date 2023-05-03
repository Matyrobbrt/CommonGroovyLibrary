/*
 * Copyright (C) 2022 GroovyMC and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.groovymc.cgl.api.datagen.recipe

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import groovy.transform.stc.SimpleType
import groovy.util.logging.Slf4j
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.CriterionTriggerInstance
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.ItemLike
import org.jetbrains.annotations.Nullable

import java.util.function.Consumer

/**
 * <p>A {@link RecipeProvider} with a few additions that better support Groovy.</p>
 * <p> All the G-recipe builders do <strong>not</strong> require an advancement criterion, unlike the vanilla recipe builders. </p>
 * <p> They are designed to be used along side closure and parenthesis-less calls to make them easier to both read and write. </p>
 * An example of how a {@link GShapelessRecipeBuilder} is inteded to be used:
 * <pre>
 * {@code
 * shapeless {
 *      result Items.SPRUCE_BUTTON * 12
 *      requires Items.AZALEA
 *
 *      category RecipeCategory.DECORATIONS
 *      group 'spruce_stuff'
 * } save 'sprucex12'
 * }
 * </pre>
 */
@Slf4j
@CompileStatic
abstract class GRecipeProvider extends RecipeProvider {
    public Consumer<FinishedRecipe> writer
    protected final String defaultNamespace

    protected final List<SaveableRecipe> forgottenRecipes = []

    /**
     * @param defaultNamespace the default namespace to be used by {@link SaveableRecipe#save(java.lang.String)}
     */
    GRecipeProvider(PackOutput packOutput, String defaultNamespace = 'minecraft') {
        super(packOutput)
        this.defaultNamespace = defaultNamespace
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        this.writer = writer
        this.buildRecipes()
        this.writer = null

        if (!forgottenRecipes.isEmpty()) {
            log.error("It seems like ${forgottenRecipes.size()} recipes created in provider ${this} have not been saved. You may save them automatically by calling GRecipeProvider#saveForgotten in buildRecipes().")
            throw new RuntimeException("${forgottenRecipes.size()} recipes have not been saved!")
        }
    }

    /**
     * Override this method to build your recipes. <br>
     * You may use the {@link #writer} as a {@code Consumer<FinishedRecipe>}, but it is not usually necessary as there are methods such as {@link #normal(net.minecraft.data.recipes.RecipeBuilder)}
     * which allow you to more easily save recipes.
     */
    protected abstract void buildRecipes()

    /**
     * Saves all forgotten recipes of type {@link BaseRecipeBuilder}. <br>
     * Note that this means the recipes will be saved to a path representing the result's registry name.
     */
    protected void saveForgotten() {
        List.copyOf(this.forgottenRecipes).each {
            if (it instanceof BaseRecipeBuilder) {
                it.save()
            }
        }
    }

    protected GSingleItemRecipeBuilder stonecutting(@DelegatesTo(value = GSingleItemRecipeBuilder, strategy = Closure.DELEGATE_FIRST) @ClosureParams(value = SimpleType, options = 'io.github.groovymc.cgl.api.datagen.GSingleItemRecipeBuilder') Closure clos) {
        recipe(new GSingleItemRecipeBuilder(RecipeSerializer.STONECUTTER), clos)
    }

    protected GSingleItemRecipeBuilder singleItem(@DelegatesTo(value = GSingleItemRecipeBuilder, strategy = Closure.DELEGATE_FIRST) @ClosureParams(value = SimpleType, options = 'io.github.groovymc.cgl.api.datagen.GSingleItemRecipeBuilder') Closure clos) {
        recipe(new GSingleItemRecipeBuilder(), clos)
    }

    protected GCookingRecipeBuilder smelting(@DelegatesTo(value = GCookingRecipeBuilder, strategy = Closure.DELEGATE_FIRST) @ClosureParams(value = SimpleType, options = 'io.github.groovymc.cgl.api.datagen.GCookingRecipeBuilder') Closure clos) {
        recipe(new GCookingRecipeBuilder(), clos)
    }

    protected GCookingRecipeBuilder blasting(@DelegatesTo(value = GCookingRecipeBuilder, strategy = Closure.DELEGATE_FIRST) @ClosureParams(value = SimpleType, options = 'io.github.groovymc.cgl.api.datagen.GCookingRecipeBuilder') Closure clos) {
        recipe(new GCookingRecipeBuilder(RecipeSerializer.BLASTING_RECIPE), clos)
    }

    protected GCookingRecipeBuilder campfireCooking(@DelegatesTo(value = GCookingRecipeBuilder, strategy = Closure.DELEGATE_FIRST) @ClosureParams(value = SimpleType, options = 'io.github.groovymc.cgl.api.datagen.GCookingRecipeBuilder') Closure clos) {
        recipe(new GCookingRecipeBuilder(RecipeSerializer.CAMPFIRE_COOKING_RECIPE), clos)
    }

    protected GCookingRecipeBuilder smoking(@DelegatesTo(value = GCookingRecipeBuilder, strategy = Closure.DELEGATE_FIRST) @ClosureParams(value = SimpleType, options = 'io.github.groovymc.cgl.api.datagen.GCookingRecipeBuilder') Closure clos) {
        recipe(new GCookingRecipeBuilder(RecipeSerializer.SMOKING_RECIPE), clos)
    }

    protected GShapedRecipeBuilder shaped(@DelegatesTo(value = GShapedRecipeBuilder, strategy = Closure.DELEGATE_FIRST) @ClosureParams(value = SimpleType, options = 'io.github.groovymc.cgl.api.datagen.GShapedRecipeBuilder') Closure clos) {
        recipe(new GShapedRecipeBuilder(), clos)
    }

    protected GShapelessRecipeBuilder shapeless(@DelegatesTo(value = GShapelessRecipeBuilder,strategy = Closure.DELEGATE_FIRST) @ClosureParams(value = SimpleType, options = 'io.github.groovymc.cgl.api.datagen.GShapelessRecipeBuilder') Closure clos) {
        recipe(new GShapelessRecipeBuilder(), clos)
    }

    protected <T extends SaveableRecipe> T recipe(@DelegatesTo.Target('recipe') Class<T> recipeClass, @DelegatesTo(target = 'recipe', genericTypeIndex = 0, strategy = Closure.DELEGATE_FIRST) @ClosureParams(FirstParam.FirstGenericType) Closure clos) {
        return recipe(recipeClass.getDeclaredConstructor().newInstance(), clos)
    }

    protected <T extends SaveableRecipe> T recipe(@DelegatesTo.Target('recipe') T recipe, @DelegatesTo(target = 'recipe', strategy = Closure.DELEGATE_FIRST) @ClosureParams(FirstParam) Closure clos) {
        recipe.provider = this
        clos.resolveStrategy = Closure.DELEGATE_FIRST
        clos.delegate = recipe
        clos(recipe)
        forgottenRecipes.add(recipe)
        return recipe
    }

    protected <T extends RecipeBuilder> SaveableRecipe normal(@DelegatesTo.Target('recipe') T recipe, @DelegatesTo(target = 'recipe', strategy = Closure.DELEGATE_FIRST) @ClosureParams(FirstParam) Closure clos = {}) {
        clos.resolveStrategy = Closure.DELEGATE_FIRST
        clos.delegate = recipe
        clos(recipe)
        return new SaveableRecipe() {
            @Override
            void save(ResourceLocation location) {
                forgottenRecipes.remove(this)
                recipe.save(getProvider().writer, location)
            }

            @Override
            GRecipeProvider getProvider() {
                return GRecipeProvider.this
            }

            @Override
            void setProvider(GRecipeProvider provider) {

            }
        }
    }
}