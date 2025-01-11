package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class DeliveryServiceTest {

    private final ConcurrentMap<UUID, OrderInfo> orders = new ConcurrentHashMap<>();
    private final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();

    private Logger setupLogger(ByteArrayOutputStream logOutputStream) {
        Logger logger = Logger.getLogger(DeliveryServiceImpl.class.getName());
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
        var order = new OrderDetails.Builder().addPancake(mock(Pancake.class))
                .withDeliveryInfo(mock(DeliveryInfo.class)).build();
        var deliveryService = new DeliveryServiceImpl(orders, deliveryQueue);
        orders.put(order.getOrderId(), new OrderInfo(order, ORDER_STATUS.PENDING));
        deliveryQueue.add(order.getOrderId());
        ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
        Logger logger = setupLogger(logOutputStream);
        Handler logHandler = logger.getHandlers()[0];
        try {
            // When
            new Thread(deliveryService).start();
            // Then
            Awaitility.await().until(orders::isEmpty);
            logHandler.flush();
            assertTrue(logOutputStream.toString().contains("Delivering order: %s".formatted(order.getOrderId())));
        } finally {
            cleanupLogger(logger, logHandler);
        }
    }

    @Test
    public void givenOrderDoesNotExist_whenTryingToDeliver_thenWarningShouldBeLogged() {
        // Given
        var orderId = UUID.randomUUID();
        var deliveryService = new DeliveryServiceImpl(orders, deliveryQueue);
        deliveryQueue.add(orderId);
        ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
        Logger logger = setupLogger(logOutputStream);
        Handler logHandler = logger.getHandlers()[0];
        try {
            // When
            new Thread(deliveryService).start();

            // Then
            Awaitility.await().until(() -> {
                logHandler.flush();
                return logOutputStream.toString().contains("Order not found: %s".formatted(orderId));
            });
        } finally {
            cleanupLogger(logger, logHandler);
        }
    }

    @Test
    public void givenOrderWithInvalidStatus_whenTryingToDeliver_thenWarningShouldBeLogged() {
        // Given
        var order = new OrderDetails.Builder().addPancake(mock(Pancake.class))
                .withDeliveryInfo(mock(DeliveryInfo.class)).build();
        var deliveryService = new DeliveryServiceImpl(orders, deliveryQueue);
        orders.put(order.getOrderId(), new OrderInfo(order, ORDER_STATUS.DELIVERED));
        deliveryQueue.add(order.getOrderId());
        ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
        Logger logger = setupLogger(logOutputStream);
        Handler logHandler = logger.getHandlers()[0];
        try {
            // When
            new Thread(deliveryService).start();
            // Then
            Awaitility.await().until(() -> {
                logHandler.flush();
                return logOutputStream.toString().contains("Invalid Status: %s".formatted(order.getOrderId()));
            });
        } finally {
            cleanupLogger(logger, logHandler);
        }
    }
}