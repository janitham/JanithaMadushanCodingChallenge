package org.pancakelab.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class OrderDetailsTest {

    @Test
    public void whenOrderDetailsIsCreatedWithoutDeliveryInfo_thenThrowException() {
        // Given
        final var builder = new OrderDetails.Builder();
        // When & Then
        assertThrows(IllegalArgumentException.class, builder::build, "DeliveryInfo is required");
    }

    @Test
    public void whenOrderDetailsIsCreatedWithoutPancakes_thenThrowException() {
        // Given
        final var builder = new OrderDetails.Builder();
        builder.withDeliveryInfo(mock(DeliveryInfo.class));
        // When & Then
        assertThrows(IllegalArgumentException.class, builder::build, "At least one pancake is required");
    }

    @Test
    public void whenOrderDetailsIsCreatedWithValidParameters_thenPancakesAreNotModifiable() {
        // Given
        final var builder = new OrderDetails.Builder();
        final var deliveryInfo = mock(DeliveryInfo.class);
        final var pancake = mock(Pancake.class);
        builder.withDeliveryInfo(deliveryInfo).addPancake(pancake);
        // When
        final var orderDetails = builder.build();
        // Then
        assertThrows(UnsupportedOperationException.class, () -> orderDetails.getPancakes().put(pancake, 2));
    }

    @Test
    public void whenAddingSimilarPancakes_thenCorrespondingCountShouldBeIncremented() {
        // Given
        final var builder = new OrderDetails.Builder();
        final var deliveryInfo = mock(DeliveryInfo.class);
        final var pancake1 = new Pancake.Builder().withChocolate(Pancake.CHOCOLATE.MILK).build();
        final var pancake2 = new Pancake.Builder().withChocolate(Pancake.CHOCOLATE.MILK).build();
        builder.withDeliveryInfo(deliveryInfo).addPancake(pancake1).addPancake(pancake2);
        // When
        final var orderDetails = builder.build();
        // Then
        assertEquals(2, orderDetails.getPancakes().get(pancake1));
    }
}