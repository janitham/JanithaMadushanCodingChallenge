package org.pancakelab.model;

public enum Privileges {
    READ('R'),
    UPDATE('U'),
    CREATE('C'),
    DELETE('D');

    private final char code;

    Privileges(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }
}
