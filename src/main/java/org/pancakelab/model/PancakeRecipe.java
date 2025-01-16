package org.pancakelab.model;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

public class PancakeRecipe implements Comparable<PancakeRecipe> {
    private final CHOCOLATE chocolate;
    private final boolean hazelNuts;
    private final boolean whippedCream;
    private final Set<String> otherIngredients;
    private final String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PancakeRecipe that = (PancakeRecipe) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    private PancakeRecipe(Builder builder) {
        this.chocolate = builder.chocolate;
        this.hazelNuts = builder.hazelNuts;
        this.whippedCream = builder.whippedCream;
        this.otherIngredients = builder.otherIngredients;
        this.name = builder.name;
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

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(PancakeRecipe o) {
        return Comparator.comparing(PancakeRecipe::getName)
                .compare(this, o);
    }

    public static class Builder {
        private CHOCOLATE chocolate;
        private boolean hazelNuts;
        private boolean whippedCream;
        private Set<String> otherIngredients;
        private String name;

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

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public PancakeRecipe build() {
            if (chocolate == null) {
                throw new IllegalArgumentException("Chocolate is required");
            }
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name is required");
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
                ", name='" + name + '\'' +
                '}';
    }

    public enum CHOCOLATE {
        MILK,
        DARK
    }
}