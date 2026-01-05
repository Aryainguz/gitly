package com.inguzdev.gitly.model;

public enum Role {
    ROLE_VIEWER("VIEWER"),
    ROLE_EDITOR("EDITOR"),
    ROLE_ADMIN("ADMIN");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
