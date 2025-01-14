package org.pancakelab.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;
import org.pancakelab.model.*;
import org.pancakelab.service.*;
import org.pancakelab.tasks.DeliveryPartnerTask;
import org.pancakelab.util.DeliveryInformationValidator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeServiceSteps {

    private static final ConcurrentMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();
    private static final ConcurrentHashMap<UUID, OrderStatus> orderStatus = new ConcurrentHashMap<>();
    private static User authenticatedUser = new User("validUser", "validPassword".toCharArray());
    private static final Thread deliveryService = new Thread(new DeliveryPartnerTask(orders, deliveryQueue, orderStatus));
    private static final ExecutorService es = Executors.newFixedThreadPool(1);
    private static final KitchenService kitchenService = new KitchenService(deliveryQueue, orders, es, orderStatus);

    private static final HashMap<String, User> systemUsers = new HashMap<>() {
        {
            put("user1", new User("user", "password".toCharArray()));
            put("user2", new User("user2", "password2".toCharArray()));
            put("user3", new User("user3", "password3".toCharArray()));
            put("user4", new User("user3", "password3".toCharArray()));
        }
    };
    private static final OrderService orderService
            = new AuthenticatedOrderService(
            new OrderServiceImpl(kitchenService, orders, orderStatus, new DeliveryInformationValidator(), deliveryQueue),
            new AuthenticationServiceImpl(
                    new HashSet<>() {
                        {
                            add(authenticatedUser);
                            addAll(systemUsers.values());
                        }
                    }
            )
    );
    private static UUID orderId;

    @Given("a disciple {string} creates an order with building {string} and room number {string}")
    public void a_disciple_creates_an_order_with_building_and_room_number(String disciple, String building, String roomNumber) throws PancakeServiceException {
        orderId = orderService.createOrder(systemUsers.get(disciple), new DeliveryInfo(roomNumber, building));
        assertNotNull(orderId);
    }

    @When("the disciple {string} adds {int} pancake of type {string}")
    public void the_disciple_adds_pancakes_of_type(String disciple, Integer count, String type) throws PancakeServiceException {
        orderService.addPancakes(systemUsers.get(disciple), orderId, Map.of(PancakeMenu.valueOf(type.toUpperCase()), count));
    }

    @When("the disciple {string} completes the order")
    public void the_disciple_completes_the_order(String disciple) throws PancakeServiceException {
        orderService.complete(systemUsers.get(disciple), orderId);
    }

    @When("the disciple {string} cancels the order")
    public void the_disciple_cancels_the_order(String disciple) throws PancakeServiceException {
        orderService.cancel(systemUsers.get(disciple), orderId);
    }

    @When("delivery partner is available for the delivery")
    public void delivery_partner_is_available_for_the_delivery() {
        if (deliveryService.getState() == Thread.State.NEW) {
            deliveryService.start();
        }
    }

    @When("delivery partner notifies delivered")
    public void delivery_partner_notifies_delivered() throws InterruptedException {
        Thread.sleep(1000);
        orderStatus.put(orderId, OrderStatus.DELIVERED);
    }

    @Then("the order status should be {string}")
    public void the_order_status_should_be(String string) {
        Awaitility.await().until(() -> orderStatus.get(orderId) == OrderStatus.valueOf(string));
    }

    @Then("the order should be removed from the database")
    public void the_order_should_be_removed_from_the_database() {
        assertThrows(AuthorizationFailureException.class, () -> orderService.orderSummary(authenticatedUser, orderId));
    }

    // Security

    @Given("an invalid orderId")
    public void an_invalid_order_id() {
        orderId = UUID.randomUUID();
    }

    @Given("a username as {string} and a password as {string}")
    public void a_username_as_and_a_password_as(String username, String password) {
        authenticatedUser = new User(username, password.toCharArray());
    }

    @When("the orderId is added to the delivery queue")
    public void the_order_id_is_added_to_the_delivery_queue() throws InterruptedException {
        deliveryQueue.put(orderId);
    }

    @When("a disciple creates an order with building {string} and room number {string} and login fails")
    public void a_disciple_creates_an_order_with_building_and_room_number_and_login_fails(String buildingNo, String roomNumber) {
        assertThrows(AuthenticationFailureException.class,
                () -> orderService.createOrder(authenticatedUser, new DeliveryInfo(roomNumber, buildingNo)));
    }

    @When("the disciple adds {int} pancake of type {string} and attempt fails")
    public void the_disciple_adds_pancake_of_type_and_attempt_fails(Integer count, String type) {
        assertThrows(AuthorizationFailureException.class,
                () -> orderService.addPancakes(authenticatedUser, orderId, Map.of(PancakeMenu.valueOf(type.toUpperCase()), count)));
    }

    @When("the disciple adds {int} pancake of type {string} and system complains large order")
    public void the_disciple_adds_pancake_of_type_and_system_complains_large_order(Integer count, String type) {
        assertThrows(PancakeServiceException.class,
                () -> orderService.addPancakes(authenticatedUser, orderId, Map.of(PancakeMenu.valueOf(type.toUpperCase()), count)));
    }

    @When("a disciple creates an order with building {string} and room number {string} and multiple orders fail")
    public void a_disciple_creates_an_order_with_building_and_room_number_and_multiple_orders_fail(String buildingNo, String roomNumber) {
        assertThrows(PancakeServiceException.class,
                () -> orderService.createOrder(authenticatedUser, new DeliveryInfo(roomNumber, buildingNo)));
    }

    @When("a disciple {string} creates an order with building {string} and room number {string} and system complains about invalid delivery information")
    public void a_disciple_creates_an_order_with_building_and_room_number_and_system_complains_about_invalid_delivery_information(
            String user, String buildingNo, String roomNumber) {
        assertThrows(ValidationException.class,
                () -> orderService.createOrder(systemUsers.get(user), new DeliveryInfo(roomNumber, buildingNo)));
    }

    @Then("the system should reject the orderId")
    public void the_system_should_reject_the_order_id() {
        Awaitility.await().until(() -> orderStatus.get(orderId) == OrderStatus.ERROR);
    }

    @Then("a disciple {string} creates an order with building {string} and room number {string} and system complains about ongoing order")
    public void a_disciple_creates_an_order_with_building_and_room_number_and_system_complains_about_ongoing_order(
            String disciple, String buildingNo, String roomNumber) {
        assertThrows(PancakeServiceException.class,
                () -> orderService.createOrder(systemUsers.get(disciple), new DeliveryInfo(roomNumber, buildingNo)));
    }
}