package org.pancakelab.service;

import org.pancakelab.tasks.DeliveryTask;

import java.util.List;
import java.util.concurrent.*;

public class DeliveryService {

    private final ExecutorService deliveryPartnerPool;

    public DeliveryService(
            final int numberOfDeliveryPartners
    ) {
        this.deliveryPartnerPool = Executors.newFixedThreadPool(numberOfDeliveryPartners);
    }

    public void registerDeliveryPartners(List<DeliveryTask> deliveryPartners) {
        for (Runnable deliveryPartner : deliveryPartners) {
            deliveryPartnerPool.submit((Runnable) deliveryPartner);
        }
    }

    public void shutdown() {
        deliveryPartnerPool.shutdown();
        try {
            if (!deliveryPartnerPool.awaitTermination(1, TimeUnit.SECONDS)) {
                for (Runnable task : deliveryPartnerPool.shutdownNow()) {
                    if (task instanceof Thread) {
                        ((Thread) task).interrupt();
                    }
                }
                if (!deliveryPartnerPool.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.err.println("DeliveryPartnerPool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            for (Runnable task : deliveryPartnerPool.shutdownNow()) {
                if (task instanceof Thread) {
                    ((Thread) task).interrupt();
                }
            }
            Thread.currentThread().interrupt();
        }
    }
}