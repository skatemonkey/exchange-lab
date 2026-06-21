package dev.exchangelab.market.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Stock {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    private final String symbol;
    private final String name;
    private final String description;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public Stock(String symbol, String name, String description) {
        this.symbol = requireText(symbol, "Stock symbol is required");
        this.name = requireText(name, "Stock name is required");
        this.description = optionalText(description);
    }

    // ---------------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------------

    private static String requireText(String value, String message) {
        Objects.requireNonNull(value, message);

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return trimmed;
    }

    private static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }
}
