package org.pancakelab.model;

import java.util.List;
import java.util.Map;

public class User {
    private final String username;
    private final char[] password;
    private final Map<String, List<Character>> privileges;

    public User(String username, char[] password, Map<String, List<Character>> privileges) {
        this.username = username;
        this.password = password;
        this.privileges = privileges;
    }

    public Map<String, List<Character>> getPrivileges() {
        return Map.copyOf(this.privileges);
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