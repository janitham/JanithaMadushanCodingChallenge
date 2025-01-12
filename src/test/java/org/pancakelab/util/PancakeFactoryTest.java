package org.pancakelab.util;

import org.junit.jupiter.api.Test;
import org.pancakelab.model.Pancake;
import org.pancakelab.model.PancakeMenu;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeFactoryTest {

    @Test
    public void whenDarkChocolateWhippedCreamHazelnutsPancake_thenIngredientsAreCorrect() {
        // Given
        Pancake pancake = PancakeFactory
                .get(PancakeMenu.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE);
        // When
        // Then
        assertEquals(pancake.getChocolate(), Pancake.CHOCOLATE.DARK);
        assertTrue(pancake.isHazelNuts());
        assertTrue(pancake.isWhippedCream());
    }

    @Test
    public void whenDarkChocolatePancake_thenIngredientsAreCorrect() {
        // Given
        Pancake pancake = PancakeFactory.get(PancakeMenu.DARK_CHOCOLATE_PANCAKE);
        // When
        // Then
        assertEquals(pancake.getChocolate(), Pancake.CHOCOLATE.DARK);
        assertFalse(pancake.isHazelNuts());
        assertFalse(pancake.isWhippedCream());
    }

    @Test
    public void whenDarkChocolateWhippedCreamPancake_thenIngredientsAreCorrect() {
        // Given
        Pancake pancake = PancakeFactory.get(PancakeMenu.DARK_CHOCOLATE_WHIP_CREAM_PANCAKE);
        // When
        // Then
        assertEquals(pancake.getChocolate(), Pancake.CHOCOLATE.DARK);
        assertFalse(pancake.isHazelNuts());
        assertTrue(pancake.isWhippedCream());
    }

    @Test
    public void whenMilkChocolateHazelnutsPancake_thenIngredientsAreCorrect() {
        // Given
        Pancake pancake = PancakeFactory.get(PancakeMenu.MILK_CHOCOLATE_HAZELNUTS_PANCAKE);
        // When
        // Then
        assertEquals(pancake.getChocolate(), Pancake.CHOCOLATE.MILK);
        assertTrue(pancake.isHazelNuts());
        assertFalse(pancake.isWhippedCream());
    }

    @Test
    public void whenMilkChocolatePancake_thenIngredientsAreCorrect() {
        // Given
        Pancake pancake = PancakeFactory.get(PancakeMenu.MILK_CHOCOLATE_PANCAKE);
        // When
        // Then
        assertEquals(pancake.getChocolate(), Pancake.CHOCOLATE.MILK);
        assertFalse(pancake.isHazelNuts());
        assertFalse(pancake.isWhippedCream());
    }
}
