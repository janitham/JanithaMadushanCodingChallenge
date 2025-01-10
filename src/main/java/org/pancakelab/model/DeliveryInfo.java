package org.pancakelab.model;

public record DeliveryInfo(String roomNo, String buildingNo) {
    public DeliveryInfo {
        if (roomNo == null || roomNo.isBlank()) {
            throw new IllegalArgumentException("Room number is required");
        }
        if (buildingNo == null || buildingNo.isBlank()) {
            throw new IllegalArgumentException("Building number is required");
        }
    }
}
