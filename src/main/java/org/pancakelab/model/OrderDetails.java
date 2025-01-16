package org.pancakelab.model;

import java.util.Map;
import java.util.UUID;

public class OrderDetails {
    public static final String DELIVERY_INFO_REQUIRED = "DeliveryInfo is required";
    public static final String PANCAKES_REQUIRED = "Order can not be completed without pancakes";

    private final DeliveryInfo deliveryInfo;
    private final Map<PancakeRecipe, Integer> pancakeItems;
    private final UUID orderId;
    private final User user;

    private OrderDetails(
            final UUID orderId,
            final DeliveryInfo deliveryInfo,
            final Map<PancakeRecipe, Integer> pancakes,
            final User user
    ) {
        this.orderId = orderId;
        this.deliveryInfo = deliveryInfo;
        this.pancakeItems = Map.copyOf(pancakes);
        this.user = user;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
    }

    public Map<PancakeRecipe, Integer> getPancakes() {
        return pancakeItems;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "OrderDetails{" +
                "deliveryInfo=" + deliveryInfo +
                ", user=" + user +
                ", pancakes=" + pancakeItems +
                '}';
    }

    public static class Builder {
        private DeliveryInfo deliveryInfo;
        private UUID orderId;
        private Map<PancakeRecipe, Integer> pancakeItems;
        private User user;

        public Builder withOrderId(final UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder withUser(final User user) {
            this.user = user;
            return this;
        }

        public Builder withDeliveryInfo(final DeliveryInfo deliveryInfo) {
            this.deliveryInfo = deliveryInfo;
            return this;
        }

        public Builder withPanCakes(final Map<PancakeRecipe, Integer> pancakeTypeIntegerMap) {
            this.pancakeItems = pancakeTypeIntegerMap;
            return this;
        }

        public OrderDetails build() {
            validateFields();
            return new OrderDetails(orderId, deliveryInfo, pancakeItems, user);
        }

        private void validateFields() {
            if (deliveryInfo == null) {
                throw new IllegalArgumentException(DELIVERY_INFO_REQUIRED);
            }
            if (orderId == null) {
                orderId = UUID.randomUUID();
            }
            if (pancakeItems == null || pancakeItems.isEmpty()) {
                throw new IllegalArgumentException(PANCAKES_REQUIRED);
            }
        }
    }
}