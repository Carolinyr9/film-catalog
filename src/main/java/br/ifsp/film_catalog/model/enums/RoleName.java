package br.ifsp.film_catalog.model.enums;

import java.util.Arrays;

public enum RoleName {
    ROLE_USER,
    ROLE_ADMIN;

    public static RoleName fromString(String value) {
        return Arrays.stream(values()).filter(c -> c.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Role Name: " + value));
    }
}
