package org.pancakelab.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeRecipeTest {

    @ParameterizedTest
    @EnumSource(PancakeRecipe.CHOCOLATE.class)
    public void shouldCreatePancakeWithCorrectIngredientsWhenChocolateIsSpecified(
            PancakeRecipe.CHOCOLATE chocolate
    ) {
        // Given
        final var pancakeBuilder = new PancakeRecipe.Builder();
        // When
        final PancakeRecipe pancakeRecipe = pancakeBuilder
                .withChocolate(chocolate)
                .build();
        // Then
        assertEquals(pancakeRecipe.getChocolate(), chocolate);
        assertFalse(pancakeRecipe.hasHazelNuts());
        assertFalse(pancakeRecipe.hasWhippedCream());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenChocolateIsNotSpecified() {
        // Given
        final var pancakeBuilder = new PancakeRecipe.Builder();
        // When
        assertThrows(IllegalArgumentException.class, pancakeBuilder::build);
    }

    @Test
    public void shouldIncludeHazelNutsWhenSpecified() {
        // Given
        final var pancakeBuilder = new PancakeRecipe.Builder();
        // When
        final PancakeRecipe pancakeRecipe = pancakeBuilder
                .withChocolate(PancakeRecipe.CHOCOLATE.MILK)
                .withHazelNuts()
                .build();
        // Then
        assertEquals(pancakeRecipe.getChocolate(), PancakeRecipe.CHOCOLATE.MILK);
        assertTrue(pancakeRecipe.hasHazelNuts());
        assertFalse(pancakeRecipe.hasWhippedCream());
    }

    @Test
    public void shouldIncludeWhippedCreamWhenSpecified() {
        // Given
        final var pancakeBuilder = new PancakeRecipe.Builder();
        // When
        final PancakeRecipe pancakeRecipe = pancakeBuilder
                .withChocolate(PancakeRecipe.CHOCOLATE.MILK)
                .withWhippedCream()
                .build();
        // Then
        assertEquals(pancakeRecipe.getChocolate(), PancakeRecipe.CHOCOLATE.MILK);
        assertFalse(pancakeRecipe.hasHazelNuts());
        assertTrue(pancakeRecipe.hasWhippedCream());
    }
}