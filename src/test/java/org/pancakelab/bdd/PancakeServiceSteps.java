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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeServiceSteps {

    private static final Map<String, List<Character>> fullPrivileges = new HashMap<>() {
        {
            put("order", List.of('C', 'R', 'U', 'D'));
            put("kitchen", List.of('C', 'R', 'U', 'D'));
            put("delivery", List.of('C', 'R', 'U', 'D'));
        }
    };
    private static final HashMap<String, User> systemUsers = new HashMap<>() {
        {
            put("user", new User("user", "password".toCharArray(), fullPrivileges));
            put("user1", new User("user1", "password1".toCharArray(), fullPrivileges));
            put("user2", new User("user2", "password2".toCharArray(), fullPrivileges));
            put("user3", new User("user3", "password3".toCharArray(), fullPrivileges));
            put("user4", new User("user4", "password4".toCharArray(), fullPrivileges));
            put("user5", new User("user5", "password5".toCharArray(), fullPrivileges));
            put("user6", new User("user5", "password6".toCharArray(), fullPrivileges));
            put("validUser", new User("validUser", "validPassword".toCharArray(), fullPrivileges));

            // Kitchen Service
            put("orderUser1", new User("orderUser1", "orderPassword1".toCharArray(), Map.of("order", List.of('C', 'R', 'U', 'D'))));
            put("hackedChef1", new User("hackedChef1", "hackedPassword1".toCharArray(), Map.of("kitchen", List.of())));

            // Delivery Service
            put("kitchenUser1", new User("kitchenUser1", "kitchenPassword1".toCharArray(), Map.of("kitchen", List.of('C', 'R', 'U', 'D'))));
            put("hackedRider1", new User("hackedRider1", "hackedPassword1".toCharArray(), Map.of("delivery", List.of())));
        }
    };
    private static final ConcurrentHashMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, OrderStatus> orderStatus = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> ordersQueue = new LinkedBlockingDeque<>();
    private static final BlockingDeque<UUID> deliveriesQueue = new LinkedBlockingDeque<>();
    private static User authenticatedUser = null;
    private static UUID orderId;

    private static final AuthenticationService authenticationService = new AuthenticationServiceImpl(
            new HashSet<>() {
                {
                    addAll(systemUsers.values());
                }
            }
    );
    private static final KitchenService kitchenService = new AuthorizedKitchenService(
            new KitchenServiceImpl(orders, orderStatus, ordersQueue, deliveriesQueue, 2),
            authenticationService
    );
    private static final DeliveryService deliveryService = new AuthorizedDeliveryService(
            new DeliveryServiceImpl(orders, orderStatus, deliveriesQueue, 2),
            authenticationService
    );

    private static final OrderService orderService = new AuthorizedOrderService(
            new OrderServiceImpl(orders, orderStatus, new DeliveryInformationValidator(), ordersQueue, 2),
            authenticationService
    );

    @Given("a disciple creates an order with building {string} and room number {string}")
    public void a_disciple_creates_an_order_with_building_and_room_number(String building, String roomNumber) throws PancakeServiceException {
        orderId = orderService.createOrder(authenticatedUser, new DeliveryInfo(roomNumber, building));
        assertNotNull(orderId);
    }

    @Given("a disciple {string} creates an order with building {string} and room number {string}")
    public void a_disciple_creates_an_order_with_building_and_room_number(String disciple, String building, String roomNumber) throws PancakeServiceException {
        orderId = orderService.createOrder(authenticatedUser, new DeliveryInfo(roomNumber, building));
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
        authenticatedUser = new User(username, password.toCharArray(), fullPrivileges);
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

    @Given("a username as {string} and a password as {string} with privileges {string}")
    public void a_username_as_and_a_password_as_with_privileges(String username, String password, String privileges) {
        Map<String, List<Character>> privilegesMap = new HashMap<>();
        if (privileges != null && !privileges.isBlank()) {
            for (String privilege : privileges.split(",")) {
                String[] parts = privilege.split("\\.");
                privilegesMap.put(parts[0], parts[1].chars().mapToObj(c -> (char) c).collect(Collectors.toList()));
            }
        }
        authenticatedUser = new User(username, password.toCharArray(), privilegesMap);
    }

    @When("accepts an order then authentication fails")
    public void accepts_an_order_then_authentication_fails() {
        assertThrows(AuthenticationFailureException.class,
                () -> kitchenService.acceptOrder(authenticatedUser, UUID.randomUUID()));
    }

    @When("notifies an order then authentication fails")
    public void notifies_an_order_then_authentication_fails() {
        assertThrows(AuthenticationFailureException.class,
                () -> kitchenService.notifyOrderCompletion(authenticatedUser, UUID.randomUUID()));
    }

    @When("accepts an order then authorization fails")
    public void accepts_an_order_then_authorization_fails() {
        assertThrows(AuthorizationFailureException.class,
                () -> kitchenService.acceptOrder(authenticatedUser, UUID.randomUUID()));
    }

    @When("notifies an order then authorization fails")
    public void notifies_an_order_then_authorization_fails() {
        assertThrows(AuthorizationFailureException.class,
                () -> kitchenService.notifyOrderCompletion(authenticatedUser, UUID.randomUUID()));
    }

    @When("accepts a delivery then authentication fails")
    public void accepts_a_delivery_then_authentication_fails() {
        assertThrows(AuthenticationFailureException.class,
                () -> deliveryService.acceptOrder(authenticatedUser, UUID.randomUUID()));
    }

    @When("sends a delivery then authentication fails")
    public void sends_a_delivery_then_authentication_fails() {
        assertThrows(AuthenticationFailureException.class,
                () -> deliveryService.sendForTheDelivery(authenticatedUser, UUID.randomUUID()));
    }

    @When("accepts a delivery then authorization fails")
    public void accepts_a_delivery_then_authorization_fails() {
        assertThrows(AuthorizationFailureException.class,
                () -> deliveryService.acceptOrder(authenticatedUser, UUID.randomUUID()));
    }

    @When("sends a delivery then authorization fails")
    public void sends_a_delivery_then_authorization_fails() {
        assertThrows(AuthorizationFailureException.class,
                () -> deliveryService.sendForTheDelivery(authenticatedUser, UUID.randomUUID()));
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