package org.pancakelab.model;

public class User {
    private final String username;
    private final char[] password;

    public User(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(username, java.util.Arrays.hashCode(password));
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username) && java.util.Arrays.equals(password, user.password);
    }
}
