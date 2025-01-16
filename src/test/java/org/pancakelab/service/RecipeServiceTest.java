package org.pancakelab.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.Pancakes;
import org.pancakelab.util.PancakeFactory;

import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeServiceTest {

    private static RecipeService recipeService;

    @BeforeAll
    static void setUp() {
        final ConcurrentSkipListSet<PancakeRecipe> repository = new ConcurrentSkipListSet<>() {{
            add(PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE));
        }};
        recipeService = new RecipeServiceImpl(repository);
    }

    @Test
    void givenValidRecipe_whenAddRecipe_thenRecipeShouldBeAdded() throws PancakeServiceException {
        // Given
        final PancakeRecipe recipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE);
        // When
        recipeService.addRecipe(recipe);
        // Then
        assertTrue(recipeService.getRecipes().contains(recipe));
    }

    @Test
    void givenExistingRecipe_whenAddRecipe_thenThrowPancakeServiceException() {
        // Given
        final PancakeRecipe recipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE);
        // When & Then
        assertThrows(PancakeServiceException.class, () -> {
                    recipeService.addRecipe(recipe);
                    recipeService.addRecipe(recipe);
                }
        );
    }

    @Test
    void givenValidRecipe_whenRemoveRecipe_thenRecipeShouldBeRemoved() throws PancakeServiceException {
        // Given
        final PancakeRecipe recipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE);
        // When
        recipeService.removeRecipe(recipe);
        // Then
        assertTrue(recipeService.getRecipes().isEmpty());
    }

    @Test
    void givenNonExistingRecipe_whenRemoveRecipe_thenShouldThrowPancakeServiceException() {
        // Given
        final PancakeRecipe recipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE);
        // When
        assertThrows(PancakeServiceException.class, () -> recipeService.removeRecipe(recipe));
    }
}
