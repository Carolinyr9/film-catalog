package br.ifsp.film_catalog.model.enums;

import java.util.Arrays;

public enum ContentRating {
    AL,
    A10,
    A12,
    A14,
    A16,
    A18,
    OTHER;

    public static ContentRating fromString(String value) {
        return Arrays.stream(values()).filter(c -> c.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Content Rating: " + value));
    }
}
