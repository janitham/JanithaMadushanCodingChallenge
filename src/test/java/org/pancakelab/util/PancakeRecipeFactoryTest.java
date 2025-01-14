package org.pancakelab.util;

import org.junit.jupiter.api.Test;
import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.Pancakes;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeRecipeFactoryTest {

    @Test
    public void whenDarkChocolateWhippedCreamHazelnutsPancake_thenIngredientsAreCorrect() {
        // Given
        PancakeRecipe pancakeRecipe = PancakeFactory
                .get(Pancakes.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE);
        // When
        // Then
        assertEquals(pancakeRecipe.getChocolate(), PancakeRecipe.CHOCOLATE.DARK);
        assertTrue(pancakeRecipe.isHazelNuts());
        assertTrue(pancakeRecipe.isWhippedCream());
    }

    @Test
    public void whenDarkChocolatePancake_thenIngredientsAreCorrect() {
        // Given
        PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE);
        // When
        // Then
        assertEquals(pancakeRecipe.getChocolate(), PancakeRecipe.CHOCOLATE.DARK);
        assertFalse(pancakeRecipe.isHazelNuts());
        assertFalse(pancakeRecipe.isWhippedCream());
    }

    @Test
    public void whenDarkChocolateWhippedCreamPancake_thenIngredientsAreCorrect() {
        // Given
        PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_WHIP_CREAM_PANCAKE);
        // When
        // Then
        assertEquals(pancakeRecipe.getChocolate(), PancakeRecipe.CHOCOLATE.DARK);
        assertFalse(pancakeRecipe.isHazelNuts());
        assertTrue(pancakeRecipe.isWhippedCream());
    }

    @Test
    public void whenMilkChocolateHazelnutsPancake_thenIngredientsAreCorrect() {
        // Given
        PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_HAZELNUTS_PANCAKE);
        // When
        // Then
        assertEquals(pancakeRecipe.getChocolate(), PancakeRecipe.CHOCOLATE.MILK);
        assertTrue(pancakeRecipe.isHazelNuts());
        assertFalse(pancakeRecipe.isWhippedCream());
    }

    @Test
    public void whenMilkChocolatePancake_thenIngredientsAreCorrect() {
        // Given
        PancakeRecipe pancakeRecipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE);
        // When
        // Then
        assertEquals(pancakeRecipe.getChocolate(), PancakeRecipe.CHOCOLATE.MILK);
        assertFalse(pancakeRecipe.isHazelNuts());
        assertFalse(pancakeRecipe.isWhippedCream());
    }
}
