package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.tasks.DeliveryPartnerTask;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class DeliveryPartnerTaskTest {

    private final ConcurrentMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();
    private final ConcurrentMap<UUID, OrderStatus> orderStatus = new ConcurrentHashMap<>();
    private final User user = new User("user", "password".toCharArray());

    private Logger setupLogger(ByteArrayOutputStream logOutputStream) {
        Logger logger = Logger.getLogger(DeliveryPartnerTask.class.getName());
        PrintStream logPrintStream = new PrintStream(logOutputStream);
        Handler logHandler = new StreamHandler(logPrintStream, new SimpleFormatter());
        logger.addHandler(logHandler);
        return logger;
    }

    private void cleanupLogger(Logger logger, Handler logHandler) {
        logger.removeHandler(logHandler);
    }

    @Test
    public void givenOrderIsPending_whenDispatched_thenItShouldBeRemovedFromTheDatabase() {
        // Given
        var order = new OrderDetails.Builder().withPanCakes(
                        Map.of(
                                Pancakes.DARK_CHOCOLATE_PANCAKE, 2
                        )
                )
                .withUser(user)
                .withDeliveryInfo(mock(DeliveryInfo.class)).build();
        var deliveryTask = new DeliveryPartnerTask(orders, deliveryQueue, orderStatus);
        orders.put(order.getOrderId(), order);
        deliveryQueue.add(order.getOrderId());
        orderStatus.put(order.getOrderId(), OrderStatus.WAITING_FOR_DELIVERY);
        ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
        Logger logger = setupLogger(logOutputStream);
        Handler logHandler = logger.getHandlers()[0];
        try {
            // When
            var thread = new Thread(deliveryTask);
            thread.start();
            // Then
            Awaitility.await().until(deliveryQueue::isEmpty);
            Awaitility.await().until(() -> {
                        orderStatus.put(order.getOrderId(), OrderStatus.DELIVERED);
                        logHandler.flush();
                        return logOutputStream.toString().contains("Delivered order: %s".formatted(order.getOrderId()));
                    }
            );
        } finally {
            cleanupLogger(logger, logHandler);
        }
    }

    @Test
    public void givenOrderDoesNotExist_whenTryingToDeliver_thenWarningShouldBeLogged() {
        // Given
        var orderId = UUID.randomUUID();
        var deliveryTask = new DeliveryPartnerTask(orders, deliveryQueue, orderStatus);
        deliveryQueue.add(orderId);
        ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
        Logger logger = setupLogger(logOutputStream);
        Handler logHandler = logger.getHandlers()[0];
        try {
            // When
            new Thread(deliveryTask).start();
            // Then
            Awaitility.await().until(() -> {
                logHandler.flush();
                return logOutputStream.toString().contains("Order not found: %s".formatted(orderId));
            });
            assertEquals(orderStatus.get(orderId), OrderStatus.ERROR);
        } finally {
            cleanupLogger(logger, logHandler);
        }
    }
}