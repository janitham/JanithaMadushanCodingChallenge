package org.pancakelab.util;

import org.pancakelab.model.PancakeRecipe;

public class PancakeFactory {

    private PancakeFactory() {
    }

    public static PancakeRecipe get(Pancakes type) {
        return switch (type) {
            case DARK_CHOCOLATE_PANCAKE ->
                    new PancakeRecipe.Builder()
                            .withChocolate(PancakeRecipe.CHOCOLATE.DARK)
                            .withName("Dark Chocolate Pancake").build();
            case DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE ->
                    new PancakeRecipe.Builder()
                            .withChocolate(PancakeRecipe.CHOCOLATE.DARK)
                            .withName("Dark Chocolate Whip Cream Hazelnuts Pancake")
                            .withWhippedCream()
                            .withHazelNuts()
                            .build();
            case DARK_CHOCOLATE_WHIP_CREAM_PANCAKE ->
                    new PancakeRecipe.Builder()
                            .withChocolate(PancakeRecipe.CHOCOLATE.DARK)
                            .withWhippedCream()
                            .withName("Dark Chocolate Whip Cream Pancake").build();
            case MILK_CHOCOLATE_HAZELNUTS_PANCAKE ->
                    new PancakeRecipe.Builder()
                            .withChocolate(PancakeRecipe.CHOCOLATE.MILK)
                            .withHazelNuts()
                            .withName("Milk Chocolate Hazelnuts Pancake")
                            .build();
            case MILK_CHOCOLATE_PANCAKE ->
                    new PancakeRecipe.Builder()
                            .withChocolate(PancakeRecipe.CHOCOLATE.MILK)
                            .withName("Milk Chocolate Pancake")
                            .build();
            default -> throw new IllegalArgumentException("Invalid pancake type");
        };
    }
}
