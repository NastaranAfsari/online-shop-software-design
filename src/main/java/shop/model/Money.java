package shop.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Helper for monetary amounts. All money in the system is a BigDecimal with scale 2. */
public final class Money {
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private Money() {
    }

    /** Validates (non-null, non-negative) and returns the amount with scale 2. */
    public static BigDecimal normalize(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /** Parses user text such as "25.5" into a normalized amount. */
    public static BigDecimal parse(String text, String fieldName) {
        try {
            return normalize(new BigDecimal(text.trim()), fieldName);
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }
}
