package org.pancakelab.util;

import org.pancakelab.model.*;

import java.util.logging.Logger;

public class PancakeUtils {

    private static final Logger logger = Logger.getLogger(PancakeUtils.class.getName());

    private PancakeUtils() {
    }

    public static void preparePancake(Pancakes type) {
        validateInputs(type);
        logPancakeDetails(PancakeFactory.get(type));
    }

    private static void validateInputs(Pancakes type) {
        if (type == null) {
            throw new IllegalArgumentException("PancakeType cannot be null");
        }
    }

    private static void logPancakeDetails(PancakeRecipe pancakeRecipe) {
        logger.info("Adding " + (pancakeRecipe.getChocolate() == PancakeRecipe.CHOCOLATE.DARK ? "Dark" : "Milk") + " chocolate...");
        if (pancakeRecipe.isWhippedCream()) {
            logger.info("Adding whipped cream...");
        }
        if (pancakeRecipe.isHazelNuts()) {
            logger.info("Adding hazelnuts...");
        }
        logger.info("%s is ready!".formatted(pancakeRecipe.toString()));
    }

    public static void notifyUser(User user, OrderStatus orderStatus){
        if(user == null || orderStatus == null){
            throw new IllegalArgumentException("User and OrderStatus cannot be null");
        }
        logger.info("Notifying %s that the order is %s".formatted(user, orderStatus));
    }

    public static void authorizeUser(User user, String service,  Character privilege) throws PancakeServiceException {
        if (!user.getPrivileges().get(service).contains(privilege)) {
            throw new PancakeServiceException("User does not have enough permission to perform this operation");
        }
    }
}
