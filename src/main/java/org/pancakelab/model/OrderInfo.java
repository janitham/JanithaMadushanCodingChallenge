package org.pancakelab.model;

public class OrderInfo {
    private final OrderDetails orderDetails;
    private ORDER_STATUS status;

    public OrderInfo(OrderDetails orderDetails, ORDER_STATUS status) {
        this.orderDetails = orderDetails;
        this.status = status;
    }

    public OrderDetails getOrderDetails() {
        return orderDetails;
    }

    public ORDER_STATUS getStatus() {
        return status;
    }

    public void setStatus(ORDER_STATUS status) {
        this.status = status;
    }
}
