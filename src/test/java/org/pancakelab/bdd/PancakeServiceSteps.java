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
    private static final ConcurrentHashMap<UUID, OrderStatus> orderStatus = new ConcurrentHashMap<>();
    private static final User authenticatedUser = new User("validUser", "validPassword".toCharArray());
    private static final Thread deliveryService
            = new Thread(new DeliveryServiceImpl(orders, deliveryQueue, orderStatus));
    private static final KitchenService kitchenService
            = KitchenServiceImpl.getInstance(1, deliveryQueue, orders, orderStatus);
    private static final OrderService orderService
            = new AuthenticatedOrderService(
            new OrderServiceImpl(kitchenService, orders, orderStatus),
            new AuthenticationServiceImpl(
                    new HashSet<>() {{
                        add(authenticatedUser);
                    }}
            )
    );
    private static UUID orderId;

    @Given("a disciple creates an order with building {string} and room number {int}")
    public void a_disciple_creates_an_order_with_building_and_room_number(String building, int roomNumber) throws PancakeServiceException {
        orderId = orderService.createOrder(authenticatedUser, new DeliveryInfo(building, String.valueOf(roomNumber)));
        assertNotNull(orderId);
    }

    @When("the disciple adds {int} pancake of type {string}")
    public void the_disciple_adds_pancakes_of_type(Integer count, String type) throws PancakeServiceException {
        orderService.addPancakes(orderId, Map.of(PancakeMenu.valueOf(type.toUpperCase()), count), authenticatedUser);
    }

    @When("the disciple completes the order")
    public void the_disciple_completes_the_order() throws PancakeServiceException {
        orderService.complete(orderId, authenticatedUser);
    }

    @When("the disciple cancels the order")
    public void the_disciple_cancels_the_order() throws PancakeServiceException {
        orderService.cancel(orderId, authenticatedUser);
    }

    @When("delivery partner is available for the delivery")
    public void delivery_partner_is_available_for_the_delivery() {
        if (deliveryService.getState() == Thread.State.NEW) {
            deliveryService.start();
        }
    }

    @Then("the order status should be {string}")
    public void the_order_status_should_be(String string) {
        Awaitility.await().until(() -> orderStatus.get(orderId) == OrderStatus.valueOf(string));
    }

    @Then("the order should be removed from the database")
    public void the_order_should_be_removed_from_the_database() {
        assertThrows(AuthorizationFailureException.class, () -> orderService.orderSummary(orderId, authenticatedUser));
    }

    // Security
    @Given("an invalid orderId")
    public void an_invalid_order_id() {
        orderId = UUID.randomUUID();
    }

    @When("the orderId is added to the delivery queue")
    public void the_order_id_is_added_to_the_delivery_queue() throws InterruptedException {
        deliveryQueue.put(orderId);
    }

    @Then("the system should reject the orderId")
    public void the_system_should_reject_the_order_id() {
        Awaitility.await().until(() -> orderStatus.get(orderId) == OrderStatus.ERROR);
    }
}