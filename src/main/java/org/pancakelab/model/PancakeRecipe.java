package org.pancakelab.model;

import java.util.Objects;

public class PancakeRecipe {
    private final CHOCOLATE chocolate;
    private final boolean hazelNuts;
    private final boolean whippedCream;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PancakeRecipe pancakeRecipe = (PancakeRecipe) o;
        return hazelNuts == pancakeRecipe.hazelNuts && whippedCream == pancakeRecipe.whippedCream && chocolate == pancakeRecipe.chocolate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chocolate, hazelNuts, whippedCream);
    }

    private PancakeRecipe(Builder builder) {
        this.chocolate = builder.chocolate;
        this.hazelNuts = builder.hazelNuts;
        this.whippedCream = builder.whippedCream;
    }

    public boolean isHazelNuts() {
        return hazelNuts;
    }

    public boolean isWhippedCream() {
        return whippedCream;
    }

    public static class Builder {
        private CHOCOLATE chocolate;
        private boolean hazelNuts;
        private boolean whippedCream;

        public Builder withChocolate(CHOCOLATE chocolate) {
            this.chocolate = chocolate;
            return this;
        }

        public Builder withHazelNuts() {
            this.hazelNuts = true;
            return this;
        }

        public Builder withWhippedCream() {
            this.whippedCream = true;
            return this;
        }

        public PancakeRecipe build() {
            if (chocolate == null) {
                throw new IllegalArgumentException("Chocolate is required");
            }
            return new PancakeRecipe(this);
        }
    }

    public CHOCOLATE getChocolate() {
        return chocolate;
    }

    public boolean hasHazelNuts() {
        return hazelNuts;
    }

    public boolean hasWhippedCream() {
        return whippedCream;
    }

    @Override
    public String toString() {
        return "Pancake [Chocolate=" + chocolate + ", Hazel Nuts=" + hazelNuts + ", Whipped Cream=" + whippedCream + "]";
    }

    public enum CHOCOLATE {
        MILK,
        DARK
    }
}