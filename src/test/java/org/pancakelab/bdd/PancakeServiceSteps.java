package org.pancakelab.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;
import org.pancakelab.model.*;
import org.pancakelab.service.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeServiceSteps {

    private static final ConcurrentMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();
    private static final ConcurrentHashMap<UUID, ORDER_STATUS> orderStatus = new ConcurrentHashMap<>();
    private static final Thread deliveryService = new Thread(new DeliveryServiceImpl(orders, deliveryQueue, orderStatus));
    private static final KitchenService kitchenService = KitchenServiceImpl.getInstance(1, deliveryQueue, orders, orderStatus);
    private static final OrderService orderService = new OrderServiceImpl(kitchenService, orders, orderStatus);

    private static UUID orderId;

    @Given("a disciple creates an order with building {string} and room number {int}")
    public void a_disciple_creates_an_order_with_building_and_room_number(String building, int roomNumber) throws PancakeServiceException {
        orderId = orderService.createOrder(new DeliveryInfo(building, String.valueOf(roomNumber)));
        assertNotNull(orderId);
    }

    @When("the disciple adds {int} pancake of type {string}")
    public void the_disciple_adds_pancakes_of_type(Integer count, String type) {
        orderService.addPancakes(orderId, Map.of(PancakeMenu.valueOf(type.toUpperCase()), count));
    }

    @When("the disciple completes the order")
    public void the_disciple_completes_the_order() {
        orderService.complete(orderId);
    }

    @When("the disciple cancels the order")
    public void the_disciple_cancels_the_order() {
        orderService.cancel(orderId);
    }

    @When("delivery partner is available for the delivery")
    public void delivery_partner_is_available_for_the_delivery() {
        deliveryService.start();
    }

    @Then("the order status should be {string}")
    public void the_order_status_should_be(String string) {
        Awaitility.await().until(()-> orderStatus.get(orderId) == ORDER_STATUS.valueOf(string));
    }

    @Then("the order should be removed from the database")
    public void the_order_should_be_removed_from_the_database() {
        assertThrows(IllegalStateException.class, () -> orderService.orderSummary(orderId));
    }

    @Then("the chef should not prepare the pancakes until the order is completed")
    public void the_chef_should_not_prepare_the_pancakes_until_the_order_is_completed() {
        //assertEquals(ORDER_STATUS.PENDING, orderService.getOrderStatus(orderId));
    }

    @Then("the chef should prepare the pancakes")
    public void the_chef_should_prepare_the_pancakes() throws ExecutionException, InterruptedException {
        //Future<ORDER_STATUS> status = orderService.complete(orderId);
        //assertEquals(ORDER_STATUS.READY_FOR_DELIVERY, status.get());
    }

    @Then("the order should be sent for delivery")
    public void the_order_should_be_sent_for_delivery() throws ExecutionException, InterruptedException {
        //Future<ORDER_STATUS> status = orderService.complete(orderId);
        //assertEquals(ORDER_STATUS.READY_FOR_DELIVERY, status.get());
    }
}