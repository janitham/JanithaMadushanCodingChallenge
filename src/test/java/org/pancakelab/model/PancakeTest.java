package org.pancakelab.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeTest {

    @ParameterizedTest
    @EnumSource(Pancake.CHOCOLATE.class)
    public void shouldCreatePancakeWithCorrectIngredientsWhenChocolateIsSpecified(
            Pancake.CHOCOLATE chocolate
    ) {
        // Given
        final var pancakeBuilder = new Pancake.Builder();
        // When
        final Pancake pancake = pancakeBuilder
                .withChocolate(chocolate)
                .build();
        // Then
        assertEquals(pancake.getChocolate(), chocolate);
        assertFalse(pancake.hasHazelNuts());
        assertFalse(pancake.hasWhippedCream());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenChocolateIsNotSpecified() {
        // Given
        final var pancakeBuilder = new Pancake.Builder();
        // When
        assertThrows(IllegalArgumentException.class, pancakeBuilder::build);
    }

    @Test
    public void shouldIncludeHazelNutsWhenSpecified() {
        // Given
        final var pancakeBuilder = new Pancake.Builder();
        // When
        final Pancake pancake = pancakeBuilder
                .withChocolate(Pancake.CHOCOLATE.MILK)
                .withHazelNuts()
                .build();
        // Then
        assertEquals(pancake.getChocolate(), Pancake.CHOCOLATE.MILK);
        assertTrue(pancake.hasHazelNuts());
        assertFalse(pancake.hasWhippedCream());
    }

    @Test
    public void shouldIncludeWhippedCreamWhenSpecified() {
        // Given
        final var pancakeBuilder = new Pancake.Builder();
        // When
        final Pancake pancake = pancakeBuilder
                .withChocolate(Pancake.CHOCOLATE.MILK)
                .withWhippedCream()
                .build();
        // Then
        assertEquals(pancake.getChocolate(), Pancake.CHOCOLATE.MILK);
        assertFalse(pancake.hasHazelNuts());
        assertTrue(pancake.hasWhippedCream());
    }
}