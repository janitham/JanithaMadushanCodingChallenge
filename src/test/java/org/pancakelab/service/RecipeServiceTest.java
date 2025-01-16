package org.pancakelab.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.util.Pancakes;
import org.pancakelab.util.PancakeFactory;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeServiceTest {

    private static RecipeService recipeService;
    private static ConcurrentHashMap<UUID, OrderDetails> ordersRepository;
    private static ConcurrentHashMap<UUID, OrderStatus> orderStatusRepository;
    private static ConcurrentSkipListSet<PancakeRecipe> recipeRepository;
    private static BlockingDeque<UUID> ordersQueue;
    private static BlockingDeque<UUID> deliveriesQueue;

    @BeforeAll
    static void setUp() {
        ordersRepository = new ConcurrentHashMap<>();
        orderStatusRepository = new ConcurrentHashMap<>();
        recipeRepository = new ConcurrentSkipListSet<>() {{
            add(PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE));
        }};
        ordersQueue = new LinkedBlockingDeque<>();
        deliveriesQueue = new LinkedBlockingDeque<>();
        recipeService = new KitchenServiceImpl(
                ordersRepository, orderStatusRepository, recipeRepository, ordersQueue, deliveriesQueue, 2);
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

    @Test
    void givenValidRecipe_whenUpdateRecipe_thenRecipeShouldBeUpdated() throws PancakeServiceException {
        // Given
        final PancakeRecipe recipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE);
        final var updated = new PancakeRecipe.Builder().withName(recipe.getName()).withChocolate(PancakeRecipe.CHOCOLATE.MILK).build();
        // When
        recipeService.updateRecipe(recipe.getName(), updated);
        // Then
        assertTrue(recipeService.getRecipes().contains(updated));
    }
}
