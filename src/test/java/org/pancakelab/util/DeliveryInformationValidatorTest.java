package org.pancakelab.util;

import org.junit.jupiter.api.Test;
import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.ValidationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeliveryInformationValidatorTest {

    private final DeliveryInformationValidator validator = new DeliveryInformationValidator();

    @Test
    public void giveNullDeliveryInfo_then_ThrowsException() {
        // Given
        // When
        // Then
        assertThrows(ValidationException.class,
                () -> validator.validate(null), DeliveryInformationValidator.NULL_DELIVERY_INFO);
    }

    @Test
    public void givenInvalidRoomNumber_then_ThrowsException() {
        // Given
        DeliveryInfo deliveryInfo = new DeliveryInfo("101", "1001");
        // When
        // Then
        assertThrows(ValidationException.class,
                () -> validator.validate(deliveryInfo), DeliveryInformationValidator.INVALID_ROOM_NUMBER);
    }

    @Test
    public void givenInvalidBuildingNumber_then_ThrowsException() {
        // Given
        DeliveryInfo deliveryInfo = new DeliveryInfo("101", "101");
        // When
        // Then
        assertThrows(ValidationException.class,
                () -> validator.validate(deliveryInfo), DeliveryInformationValidator.INVALID_BUILDING_NUMBER);
    }

    @Test
    public void givenValidDeliveryInfo_then_NoException() throws ValidationException {
        // Given
        DeliveryInfo deliveryInfo = new DeliveryInfo("101", "10");
        // When
        // Then
        validator.validate(deliveryInfo);
    }
}
