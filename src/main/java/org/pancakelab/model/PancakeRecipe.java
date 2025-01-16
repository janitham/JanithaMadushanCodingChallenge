package org.pancakelab.model;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

public class PancakeRecipe implements Comparable<PancakeRecipe> {
    private final CHOCOLATE chocolate;
    private final boolean hazelNuts;
    private final boolean whippedCream;
    private final Set<String> otherIngredients;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PancakeRecipe that = (PancakeRecipe) o;
        return hazelNuts == that.hazelNuts && whippedCream == that.whippedCream
                && chocolate == that.chocolate && Objects.equals(otherIngredients, that.otherIngredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chocolate, hazelNuts, whippedCream, otherIngredients);
    }

    private PancakeRecipe(Builder builder) {
        this.chocolate = builder.chocolate;
        this.hazelNuts = builder.hazelNuts;
        this.whippedCream = builder.whippedCream;
        this.otherIngredients = builder.otherIngredients;
    }

    public boolean hasHazelNuts() {
        return hazelNuts;
    }

    public boolean hasWhippedCream() {
        return whippedCream;
    }

    public Set<String> getOtherIngredients() {
        return otherIngredients == null ? Set.of() : Set.copyOf(otherIngredients);
    }

    @Override
    public int compareTo(PancakeRecipe o) {
        return Comparator.comparing(PancakeRecipe::getChocolate)
                .thenComparing(PancakeRecipe::hasWhippedCream)
                .thenComparing(PancakeRecipe::hasHazelNuts)
                .thenComparing(PancakeRecipe::getOtherIngredients, Comparator.comparing(Set::toString))
                .compare(this, o);
    }

    public static class Builder {
        private CHOCOLATE chocolate;
        private boolean hazelNuts;
        private boolean whippedCream;
        private Set<String> otherIngredients;

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

        public Builder withOtherIngredients(Set<String> otherIngredients) {
            this.otherIngredients = Set.copyOf(otherIngredients);
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

    @Override
    public String toString() {
        return "PancakeRecipe{" +
                "chocolate=" + chocolate +
                ", hazelNuts=" + hazelNuts +
                ", whippedCream=" + whippedCream +
                ", otherIngredients=" + otherIngredients +
                '}';
    }

    public enum CHOCOLATE {
        MILK,
        DARK
    }
}