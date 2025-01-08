package org.pancakelab.model;

public class Pancake {
    private final CHOCOLATE chocolate;
    private final boolean hazelNuts;
    private final boolean whippedCream;

    private Pancake(Builder builder) {
        this.chocolate = builder.chocolate;
        this.hazelNuts = builder.hazelNuts;
        this.whippedCream = builder.whippedCream;
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

        public Pancake build() {
            if (chocolate == null) {
                throw new IllegalArgumentException("Chocolate is required");
            }
            return new Pancake(this);
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