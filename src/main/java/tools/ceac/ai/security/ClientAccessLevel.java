package tools.ceac.ai.security;

import java.util.Locale;

/**
 * Effective access profile granted to the desktop session.
 */
public enum ClientAccessLevel {
    READ_ONLY,
    READ_WRITE;

    public boolean allowsWrites() {
        return this == READ_WRITE;
    }

    public String displayName() {
        return switch (this) {
            case READ_ONLY -> "solo lectura";
            case READ_WRITE -> "lectura y escritura";
        };
    }

    public static ClientAccessLevel fromValue(String value) {
        if (value == null || value.isBlank()) {
            return READ_WRITE;
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "READ_ONLY" -> READ_ONLY;
            case "READ_WRITE" -> READ_WRITE;
            default -> throw new IllegalArgumentException("Unsupported CEAC access level: " + value);
        };
    }
}
