package org.pancakelab.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeTest {

    @ParameterizedTest
    @EnumSource(Pancake.CHOCOLATE.class)
    public void When_Pancake_Expect_CorrectIngredients(
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
    public void When_Pancake_not_specify_chocolate_Then_throw_illegal_argument_exception() {
        // Given
        final var pancakeBuilder = new Pancake.Builder();

        // When
        assertThrows(IllegalArgumentException.class, pancakeBuilder::build);
    }

    @Test
    public void When_Pancake_specify_hazelNuts_Expect_HazelNuts() {
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
    public void When_Pancake_specify_whippedCream_Expect_WhippedCream() {
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