package org.pancakelab.util;

import org.junit.jupiter.api.Test;
import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.Pancakes;

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
        assertTrue(pancakeRecipe.isHazelNuts());
        assertTrue(pancakeRecipe.isWhippedCream());
    }

    @Test
    void whenDarkChocolatePancake_thenIngredientsAreCorrect() {
        // Given
        final PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE);
        // When
        // Then
        assertEquals(PancakeRecipe.CHOCOLATE.DARK, pancakeRecipe.getChocolate());
        assertFalse(pancakeRecipe.isHazelNuts());
        assertFalse(pancakeRecipe.isWhippedCream());
    }

    @Test
    void whenDarkChocolateWhippedCreamPancake_thenIngredientsAreCorrect() {
        // Given
        final PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_WHIP_CREAM_PANCAKE);
        // When
        // Then
        assertEquals(PancakeRecipe.CHOCOLATE.DARK, pancakeRecipe.getChocolate());
        assertFalse(pancakeRecipe.isHazelNuts());
        assertTrue(pancakeRecipe.isWhippedCream());
    }

    @Test
    void whenMilkChocolateHazelnutsPancake_thenIngredientsAreCorrect() {
        // Given
        final PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_HAZELNUTS_PANCAKE);
        // When
        // Then
        assertEquals(PancakeRecipe.CHOCOLATE.MILK, pancakeRecipe.getChocolate());
        assertTrue(pancakeRecipe.isHazelNuts());
        assertFalse(pancakeRecipe.isWhippedCream());
    }

    @Test
    void whenMilkChocolatePancake_thenIngredientsAreCorrect() {
        // Given
        final PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE);
        // When
        // Then
        assertEquals(PancakeRecipe.CHOCOLATE.MILK, pancakeRecipe.getChocolate());
        assertFalse(pancakeRecipe.isHazelNuts());
        assertFalse(pancakeRecipe.isWhippedCream());
    }
}
