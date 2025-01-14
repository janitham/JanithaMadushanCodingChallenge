package org.pancakelab.util;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.ValidationException;
import org.pancakelab.model.Validator;

/**
 * Assumption: Each building has a unique building number and each room has a unique room number.
 * And building numbers and room numbers should be in a range.
 * This class validates the delivery information provided by the user.
 * The room number and building number are mandatory fields.
 */
public class DeliveryInformationValidator implements Validator<DeliveryInfo> {
    public static final String NULL_DELIVERY_INFO = "DeliveryInfo cannot be null";
    public static final String INVALID_ROOM_NUMBER = "Invalid room number";
    public static final String INVALID_BUILDING_NUMBER = "Invalid building number";

    @Override
    public void validate(DeliveryInfo deliveryInfo) throws ValidationException {
        if (deliveryInfo == null) {
            throw new ValidationException(NULL_DELIVERY_INFO);
        }
        if (deliveryInfo.roomNo() == null || deliveryInfo.roomNo().isEmpty() ||
                !isValidRoomNumber(deliveryInfo.roomNo())) {
            throw new ValidationException(INVALID_ROOM_NUMBER);
        }
        if (deliveryInfo.buildingNo() == null || deliveryInfo.buildingNo().isEmpty() ||
                !isValidBuildingNumber(deliveryInfo.buildingNo())) {
            throw new ValidationException(INVALID_BUILDING_NUMBER);
        }
    }

    /**
     * Validates the room number, just validates if it is in a valid range.
     *
     * @param roomNumber
     * @return
     */
    private boolean isValidRoomNumber(String roomNumber) {
        try {
            int roomNo = Integer.parseInt(roomNumber);
            return roomNo > 0 && roomNo <= 1000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates the building number, just validates if it is in a valid range.
     *
     * @param buildingNumber
     * @return
     */
    private boolean isValidBuildingNumber(String buildingNumber) {
        try {
            int buildingNo = Integer.parseInt(buildingNumber);
            return buildingNo > 0 && buildingNo <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}