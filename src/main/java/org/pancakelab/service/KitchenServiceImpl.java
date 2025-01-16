package org.pancakelab.service;

import org.pancakelab.model.*;
import org.pancakelab.util.PancakeUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Implementation of the KitchenService interface.
 * This service handles the processing of orders in the kitchen, including accepting orders,
 * updating their status, and notifying users upon completion.
 * It uses a separate thread to update the local order map and manages order and delivery queues.
 */
public class KitchenServiceImpl implements ChefService, RecipeService {

    public static final String RECIPE_ALREADY_EXISTS = "Recipe already exists.";
    public static final String RECIPE_DOES_NOT_EXIST = "Recipe does not exist.";
    public static final String RECIPE_CANNOT_BE_NULL = "Recipe cannot be null.";

    private final ConcurrentMap<UUID, OrderDetails> ordersRepository;
    private final ConcurrentMap<UUID, OrderStatus> orderStatusRepository;
    private final ConcurrentSkipListSet<PancakeRecipe> pancakeRecipesRepository;
    private final ExecutorService executorService;
    private final BlockingDeque<UUID> orderQueue;
    private final BlockingDeque<UUID> deliveryQueue;
    private final Map<UUID, Map<PancakeRecipe, Integer>> localOrderMap;

    /**
     * Constructs a new KitchenServiceImpl.
     *
     * @param ordersRepository      the map of order details
     * @param orderStatusRepository the map of order statuses
     * @param orderQueue            the queue of orders to be processed
     * @param deliveryQueue         the queue of orders ready for delivery
     * @param internalThreads       the number of internal threads to use
     */
    public KitchenServiceImpl(
            final ConcurrentMap<UUID, OrderDetails> ordersRepository,
            final ConcurrentMap<UUID, OrderStatus> orderStatusRepository,
            final ConcurrentSkipListSet<PancakeRecipe> pancakeRecipesRepository,
            final BlockingDeque<UUID> orderQueue,
            final BlockingDeque<UUID> deliveryQueue,
            final Integer internalThreads
    ) {
        this.ordersRepository = ordersRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.pancakeRecipesRepository = pancakeRecipesRepository;
        this.orderQueue = orderQueue;
        this.deliveryQueue = deliveryQueue;
        this.executorService = Executors.newFixedThreadPool(internalThreads);
        this.localOrderMap = new ConcurrentHashMap<>();
        startOrderUpdateThread();
    }

    /**
     * Starts a thread to update the local order map with orders from the order queue.
     */
    private void startOrderUpdateThread() {
        executorService.submit(() -> {
            while (true) {
                try {
                    UUID orderId = orderQueue.take();
                    synchronized (localOrderMap) {
                        updateLocalOrderMap(orderId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * Allows the user to view the current orders.
     *
     * @param user the user requesting to view orders
     * @return a map of order IDs to pancake recipes and their quantities
     */
    @Override
    public Map<UUID, Map<PancakeRecipe, Integer>> viewOrders(User user) {
        return new ConcurrentHashMap<>(localOrderMap);
    }

    /**
     * Allows the user to accept an order. The order status is updated asynchronously.
     *
     * @param user    the user accepting the order
     * @param orderId the ID of the order to be accepted
     */
    @Override
    public void acceptOrder(User user, UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails;
            synchronized (ordersRepository) {
                orderDetails = ordersRepository.get(orderId);
            }
            if (orderDetails != null) {
                synchronized (orderStatusRepository) {
                    orderStatusRepository.put(orderId, OrderStatus.IN_PROGRESS);
                }
                PancakeUtils.notifyUser(user, OrderStatus.IN_PROGRESS);
            }
        }, executorService);
    }

    /**
     * Notifies the user that the order is complete and ready for delivery. The order status is updated asynchronously.
     *
     * @param user    the user to be notified
     * @param orderId the ID of the order that is complete
     */
    @Override
    public void notifyOrderCompletion(User user, UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails;
            synchronized (ordersRepository) {
                orderDetails = ordersRepository.get(orderId);
            }
            if (orderDetails != null) {
                synchronized (orderStatusRepository) {
                    orderStatusRepository.put(orderId, OrderStatus.READY_FOR_DELIVERY);
                }
                PancakeUtils.notifyUser(user, OrderStatus.READY_FOR_DELIVERY);
                synchronized (deliveryQueue) {
                    deliveryQueue.add(orderId);
                }
                synchronized (localOrderMap) {
                    localOrderMap.remove(orderId);
                }
            }
        }, executorService);
    }

    /**
     * Updates the local order map with the details of the specified order.
     *
     * @param orderId the ID of the order to be updated
     */
    private void updateLocalOrderMap(UUID orderId) {
        synchronized (ordersRepository) {
            OrderDetails orderDetails = ordersRepository.get(orderId);
            if (orderDetails != null) {
                Map<PancakeRecipe, Integer> pancakeRecipes = new ConcurrentHashMap<>(orderDetails.getPancakes());
                localOrderMap.put(orderId, pancakeRecipes);
            }
        }
    }


    /**
     * Adds a new pancake recipe to the repository.
     *
     * @param user   the user adding the recipe
     * @param recipe the pancake recipe to be added
     * @throws PancakeServiceException if the recipe already exists or is null
     */
    @Override
    public void addRecipe(User user, PancakeRecipe recipe) throws PancakeServiceException {
        validate(recipe);
        if (!pancakeRecipesRepository.add(recipe)) {
            throw new PancakeServiceException(RECIPE_ALREADY_EXISTS);
        }
    }

    /**
     * Removes a pancake recipe from the repository.
     *
     * @param user   the user removing the recipe
     * @param recipe the pancake recipe to be removed
     * @throws PancakeServiceException if the recipe does not exist
     */
    @Override
    public void removeRecipe(User user, String recipe) throws PancakeServiceException {
        if (!pancakeRecipesRepository.removeIf(r -> r.getName().equals(recipe))) {
            throw new PancakeServiceException(RECIPE_DOES_NOT_EXIST);
        }
    }

    /**
     * Updates an existing pancake recipe in the repository.
     *
     * @param user   the user updating the recipe
     * @param name   the name of the recipe to be updated
     * @param recipe the new pancake recipe
     * @throws PancakeServiceException if the recipe is null
     */
    @Override
    public void updateRecipe(User user, String name, PancakeRecipe recipe) throws PancakeServiceException {
        validate(recipe);
        pancakeRecipesRepository.removeIf(r -> r.getName().equals(name));
        pancakeRecipesRepository.add(recipe);
    }

    /**
     * Checks if a pancake recipe exists in the repository.
     *
     * @param user   the user checking the recipe
     * @param recipe the pancake recipe to check
     * @throws PancakeServiceException if the recipe does not exist
     */
    @Override
    public void exits(User user, PancakeRecipe recipe) throws PancakeServiceException {
        if (!pancakeRecipesRepository.contains(recipe)) {
            throw new PancakeServiceException(RECIPE_DOES_NOT_EXIST);
        }
    }

    /**
     * Retrieves all pancake recipes from the repository.
     *
     * @param user the user requesting the recipes
     * @return a set of all pancake recipes
     */
    @Override
    public Set<PancakeRecipe> getRecipes(User user) {
        return new HashSet<>(pancakeRecipesRepository);
    }

    /**
     * Validates the given pancake recipe.
     *
     * @param recipe the pancake recipe to validate
     * @throws PancakeServiceException if the recipe is null
     */
    private void validate(PancakeRecipe recipe) throws PancakeServiceException {
        if (recipe == null) {
            throw new PancakeServiceException(RECIPE_CANNOT_BE_NULL);
        }
    }

    /**
     * Shuts down the executor service, waiting for tasks to complete or forcing shutdown if necessary.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}