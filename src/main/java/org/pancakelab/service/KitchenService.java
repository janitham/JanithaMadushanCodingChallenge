package org.pancakelab.service;

import org.pancakelab.tasks.PreparationTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KitchenService {
    private final ExecutorService deliveryExecutor;

    public KitchenService(
            final int numberOfChefsInTheKitchen
    ) {
        this.deliveryExecutor = Executors.newFixedThreadPool(numberOfChefsInTheKitchen);
    }

    public void submitTask(PreparationTask preparationTask){
        deliveryExecutor.submit(preparationTask);
    }

    public void shutdown() {
        deliveryExecutor.shutdown();
        try {
            if (!deliveryExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                deliveryExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            deliveryExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}