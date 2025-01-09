package org.pancakelab.model;

import org.pancakelab.model.pancakes.PancakeRecipe;

import java.util.List;

public class OrderDetails {
    private DeliveryInfo deliveryInfo;
    private List<Pancake> pancakes;

    /*
    this order can be completed or cancelled
    and the chef can get and complete the order
    and send this to the delivery to the delivery infor provided via a carrrier
    i want to do this as a rich domain model using queues of java to simulate queues in tthe real world
     */

    @Override
    public String toString() {
        return "1";
    }
}
