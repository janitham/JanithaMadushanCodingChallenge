package org.pancakelab.util;

import org.pancakelab.model.Pancake;
import org.pancakelab.model.PancakeMenu;

import java.util.logging.Logger;

public class PancakeUtils {

    private static final Logger logger = Logger.getLogger(PancakeUtils.class.getName());

    private PancakeUtils() {
    }

    public static void preparePancake(PancakeMenu type) {
        validateInputs(type);
        Pancake pancake = PancakeFactory.get(type);
        logPancakeDetails(pancake);
    }

    private static void validateInputs(PancakeMenu type) {
        if (type == null) {
            throw new IllegalArgumentException("PancakeType cannot be null");
        }
    }

    private static void logPancakeDetails(Pancake pancake) {
        logger.info("Adding " + (pancake.getChocolate() == Pancake.CHOCOLATE.DARK ? "Dark" : "Milk") + " chocolate...");
        if (pancake.isWhippedCream()) {
            logger.info("Adding whipped cream...");
        }
        if (pancake.isHazelNuts()) {
            logger.info("Adding hazelnuts...");
        }
        logger.info("%s is ready!".formatted(pancake.toString()));
    }
}
