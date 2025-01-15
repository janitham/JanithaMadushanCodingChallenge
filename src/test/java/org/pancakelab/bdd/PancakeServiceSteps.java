package org.pancakelab.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;
import org.pancakelab.model.*;
import org.pancakelab.service.*;
import org.pancakelab.util.DeliveryInformationValidator;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeServiceSteps {

    private static final Map<String, List<Character>> privileges = new HashMap<>() {
        {
            put("order", List.of('C', 'R', 'U', 'D'));
            put("kitchen", List.of('C', 'R', 'U', 'D'));
            put("delivery", List.of('C', 'R', 'U', 'D'));
        }
    };
    private static final HashMap<String, User> systemUsers = new HashMap<>() {
        {
            put("user1", new User("user", "password".toCharArray(), privileges));
            put("user2", new User("user2", "password2".toCharArray(), privileges));
            put("user3", new User("user3", "password3".toCharArray(), privileges));
            put("user4", new User("user4", "password4".toCharArray(), privileges));
            put("user5", new User("user5", "password5".toCharArray(), privileges));
            put("user6", new User("user5", "password6".toCharArray(), privileges));
        }
    };
    private static final ConcurrentHashMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, OrderStatus> orderStatus = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> ordersQueue = new LinkedBlockingDeque<>();
    private static User authenticatedUser = new User("validUser", "validPassword".toCharArray(), privileges);
    private static UUID orderId;


    private static final AuthenticationService authenticationService = new AuthenticationServiceImpl(
            new HashSet<>() {
                {
                    add(authenticatedUser);
                    addAll(systemUsers.values());
                }
            }
    );
    private static final KitchenService kitchenService = new AuthorizedKitchenService(
            new KitchenServiceImpl(orders, orderStatus, ordersQueue),
            authenticationService
    );
    private static final DeliveryService deliveryService = new AuthorizedDeliveryService(
            new DeliveryServiceImpl(orders, orderStatus),
            authenticationService
    );

    private static final OrderService orderService = new AuthorizedOrderService(
            new OrderServiceImpl(orders, orderStatus, new DeliveryInformationValidator(), ordersQueue),
            authenticationService
    );

    @Given("a disciple {string} creates an order with building {string} and room number {string}")
    public void a_disciple_creates_an_order_with_building_and_room_number(String disciple, String building, String roomNumber) throws PancakeServiceException {
        orderId = orderService.createOrder(systemUsers.get(disciple), new DeliveryInfo(roomNumber, building));
        assertNotNull(orderId);
    }

    @When("the disciple {string} adds {int} pancake of type {string}")
    public void the_disciple_adds_pancakes_of_type(String disciple, Integer count, String type) throws PancakeServiceException {
        orderService.addPancakes(systemUsers.get(disciple), orderId, Map.of(Pancakes.valueOf(type.toUpperCase()), count));
    }

    @When("the disciple {string} completes the order")
    public void the_disciple_completes_the_order(String disciple) throws PancakeServiceException {
        orderService.complete(systemUsers.get(disciple), orderId);
    }

    @When("the disciple {string} cancels the order")
    public void the_disciple_cancels_the_order(String disciple) throws PancakeServiceException {
        orderService.cancel(systemUsers.get(disciple), orderId);
    }

    @Then("the order status should be {string}")
    public void the_order_status_should_be(String string) {
        Awaitility.await().until(() -> orderStatus.get(orderId) == OrderStatus.valueOf(string));
    }

    @Then("the order should be removed from the database")
    public void the_order_should_be_removed_from_the_database() {
        assertThrows(AuthorizationFailureException.class, () -> orderService.orderSummary(authenticatedUser, orderId));
    }

    @When("the chef {string} accepts the order")
    public void the_chef_accepts_the_order(String user) throws PancakeServiceException {
        kitchenService.acceptOrder(systemUsers.get(user), orderId);
    }

    @Given("a disciple {string} has a created order")
    public void a_disciple_has_a_created_order(String user) {
        addOrderToTheSystem(user, OrderStatus.CREATED);
    }

    @Given("a disciple {string} has an order in progress")
    public void a_disciple_has_an_order_in_progress(String user) {
        addOrderToTheSystem(user, OrderStatus.IN_PROGRESS);
    }

    @When("the chef {string} completes the order")
    public void the_chef_completes_the_order(String user) throws PancakeServiceException {
        kitchenService.notifyOrderCompletion(systemUsers.get(user), orderId);
    }

    @Given("a disciple {string} has an order ready for delivery")
    public void a_disciple_has_an_order_ready_for_delivery(String user) {
        addOrderToTheSystem(user, OrderStatus.READY_FOR_DELIVERY);
    }

    @When("the rider {string} accepts the order")
    public void the_rider_accepts_the_order(String user) throws PancakeServiceException {
        deliveryService.acceptOrder(systemUsers.get(user), orderId);
    }


    @Given("a disciple {string} has an order out for delivery")
    public void a_disciple_has_an_order_out_for_delivery(String user) {
        addOrderToTheSystem(user, OrderStatus.OUT_FOR_DELIVERY);
    }

    @When("the rider {string} completes the order")
    public void the_rider_completes_the_order(String user) throws PancakeServiceException {
        deliveryService.sendForTheDelivery(systemUsers.get(user), orderId);
    }

    // Security

    @Given("an invalid orderId")
    public void an_invalid_order_id() {
        orderId = UUID.randomUUID();
    }

    @Given("a username as {string} and a password as {string}")
    public void a_username_as_and_a_password_as(String username, String password) {
        authenticatedUser = new User(username, password.toCharArray(), privileges);
    }

    @When("a disciple creates an order with building {string} and room number {string} and login fails")
    public void a_disciple_creates_an_order_with_building_and_room_number_and_login_fails(String buildingNo, String roomNumber) {
        assertThrows(AuthenticationFailureException.class,
                () -> orderService.createOrder(authenticatedUser, new DeliveryInfo(roomNumber, buildingNo)));
    }

    @When("the disciple adds {int} pancake of type {string} and attempt fails")
    public void the_disciple_adds_pancake_of_type_and_attempt_fails(Integer count, String type) {
        assertThrows(AuthorizationFailureException.class,
                () -> orderService.addPancakes(authenticatedUser, orderId, Map.of(Pancakes.valueOf(type.toUpperCase()), count)));
    }

    @When("the disciple adds {int} pancake of type {string} and system complains large order")
    public void the_disciple_adds_pancake_of_type_and_system_complains_large_order(Integer count, String type) {
        assertThrows(PancakeServiceException.class,
                () -> orderService.addPancakes(authenticatedUser, orderId, Map.of(Pancakes.valueOf(type.toUpperCase()), count)));
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

    private void addOrderToTheSystem(String user, OrderStatus status) {
        orderId = UUID.randomUUID();
        orders.put(
                orderId,
                new OrderDetails.Builder()
                        .withOrderId(orderId)
                        .withUser(systemUsers.get(user))
                        .withDeliveryInfo(new DeliveryInfo("1", "2")).withPanCakes(
                                Map.of(Pancakes.DARK_CHOCOLATE_PANCAKE, 1)
                        ).build());
        orderStatus.put(orderId, status);
    }
}