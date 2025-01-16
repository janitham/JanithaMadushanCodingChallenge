package org.pancakelab.util;

import org.junit.jupiter.api.Test;
import org.pancakelab.model.PancakeRecipe;

import static org.junit.jupiter.api.Assertions.*;

class PancakeRecipeFactoryTest {

    @Test
    void whenDarkChocolateWhippedCreamHazelnutsPancake_thenIngredientsAreCorrect() {
        // Given
        final PancakeRecipe pancakeRecipe = PancakeFactory
                .get(Pancakes.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE);
        // When
        // Then
        assertEquals(PancakeRecipe.CHOCOLATE.DARK, pancakeRecipe.getChocolate());
        assertTrue(pancakeRecipe.hasHazelNuts());
        assertTrue(pancakeRecipe.hasWhippedCream());
    }

    @Test
    void whenDarkChocolatePancake_thenIngredientsAreCorrect() {
        // Given
        final PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE);
        // When
        // Then
        assertEquals(PancakeRecipe.CHOCOLATE.DARK, pancakeRecipe.getChocolate());
        assertFalse(pancakeRecipe.hasHazelNuts());
        assertFalse(pancakeRecipe.hasWhippedCream());
    }

    @Test
    void whenDarkChocolateWhippedCreamPancake_thenIngredientsAreCorrect() {
        // Given
        final PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_WHIP_CREAM_PANCAKE);
        // When
        // Then
        assertEquals(PancakeRecipe.CHOCOLATE.DARK, pancakeRecipe.getChocolate());
        assertFalse(pancakeRecipe.hasHazelNuts());
        assertTrue(pancakeRecipe.hasWhippedCream());
    }

    @Test
    void whenMilkChocolateHazelnutsPancake_thenIngredientsAreCorrect() {
        // Given
        final PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_HAZELNUTS_PANCAKE);
        // When
        // Then
        assertEquals(PancakeRecipe.CHOCOLATE.MILK, pancakeRecipe.getChocolate());
        assertTrue(pancakeRecipe.hasHazelNuts());
        assertFalse(pancakeRecipe.hasWhippedCream());
    }

    @Test
    void whenMilkChocolatePancake_thenIngredientsAreCorrect() {
        // Given
        final PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE);
        // When
        // Then
        assertEquals(PancakeRecipe.CHOCOLATE.MILK, pancakeRecipe.getChocolate());
        assertFalse(pancakeRecipe.hasHazelNuts());
        assertFalse(pancakeRecipe.hasWhippedCream());
    }
}
