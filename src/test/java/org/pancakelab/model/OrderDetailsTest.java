package org.pancakelab.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.pancakelab.model.OrderDetails.DELIVERY_INFO_REQUIRED;
import static org.pancakelab.model.OrderDetails.PANCAKES_REQUIRED;

public class OrderDetailsTest {

    @Test
    public void givenNoDeliveryInfo_whenBuildingOrderDetails_thenThrowException() {
        // Given
        OrderDetails.Builder builder = new OrderDetails.Builder();
        // When & Then
        assertThrows(IllegalArgumentException.class, builder::build, DELIVERY_INFO_REQUIRED);
    }

    @Test
    public void givenNoPancakes_whenBuildingOrderDetails_thenThrowException() {
        // Given
        OrderDetails.Builder builder = new OrderDetails.Builder();
        builder.withDeliveryInfo(mock(DeliveryInfo.class));
        // When & Then
        assertThrows(IllegalArgumentException.class, builder::build, PANCAKES_REQUIRED);
    }

    @Test
    public void givenValidParameters_whenBuildingOrderDetails_thenPancakesAreNotModifiable() {
        // Given
        OrderDetails.Builder builder = new OrderDetails.Builder();
        DeliveryInfo deliveryInfo = mock(DeliveryInfo.class);
        builder.withDeliveryInfo(deliveryInfo).withPanCakes(Map.of(Pancakes.DARK_CHOCOLATE_PANCAKE, 2));
        // When
        OrderDetails orderDetails = builder.build();
        // Then
        assertNotNull(orderDetails.getPancakes());
        assertNotNull(orderDetails.getDeliveryInfo());
        assertNotNull(orderDetails.getOrderId());
    }
}