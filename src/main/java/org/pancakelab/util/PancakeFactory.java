package org.pancakelab.util;

import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.PancakeMenu;

public class PancakeFactory {

    private PancakeFactory() {
    }

    public static PancakeRecipe get(PancakeMenu type) {
        return switch (type) {
            case DARK_CHOCOLATE_PANCAKE ->
                    new PancakeRecipe.Builder().withChocolate(PancakeRecipe.CHOCOLATE.DARK).build();
            case DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE ->
                    new PancakeRecipe.Builder().withChocolate(PancakeRecipe.CHOCOLATE.DARK).withWhippedCream().withHazelNuts().build();
            case DARK_CHOCOLATE_WHIP_CREAM_PANCAKE ->
                    new PancakeRecipe.Builder().withChocolate(PancakeRecipe.CHOCOLATE.DARK).withWhippedCream().build();
            case MILK_CHOCOLATE_HAZELNUTS_PANCAKE ->
                    new PancakeRecipe.Builder().withChocolate(PancakeRecipe.CHOCOLATE.MILK).withHazelNuts().build();
            case MILK_CHOCOLATE_PANCAKE ->
                    new PancakeRecipe.Builder().withChocolate(PancakeRecipe.CHOCOLATE.MILK).build();
            default -> throw new IllegalArgumentException("Invalid pancake type");
        };
    }
}
