package org.pancakelab.util;

import org.pancakelab.model.Pancake;
import org.pancakelab.model.PancakeMenu;

public class PancakeFactory {

    private PancakeFactory() {
    }

    public static Pancake get(PancakeMenu type) {
        return switch (type) {
            case DARK_CHOCOLATE_PANCAKE ->
                    new Pancake.Builder().withChocolate(Pancake.CHOCOLATE.DARK).build();
            case DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE ->
                    new Pancake.Builder().withChocolate(Pancake.CHOCOLATE.DARK).withWhippedCream().withHazelNuts().build();
            case DARK_CHOCOLATE_WHIP_CREAM_PANCAKE ->
                    new Pancake.Builder().withChocolate(Pancake.CHOCOLATE.DARK).withWhippedCream().build();
            case MILK_CHOCOLATE_HAZELNUTS_PANCAKE ->
                    new Pancake.Builder().withChocolate(Pancake.CHOCOLATE.MILK).withHazelNuts().build();
            case MILK_CHOCOLATE_PANCAKE ->
                    new Pancake.Builder().withChocolate(Pancake.CHOCOLATE.MILK).build();
            default -> throw new IllegalArgumentException("Invalid pancake type");
        };
    }
}
