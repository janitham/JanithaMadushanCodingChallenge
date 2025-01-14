package org.pancakelab.model;

public enum OrderStatus {
    CREATED,
    READY_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    COMPLETED,
    WAITING_FOR_DELIVERY,
    DELIVERY_PARTNER_ASSIGNED,
    ERROR
}
