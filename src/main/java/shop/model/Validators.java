package shop.model;

import java.util.regex.Pattern;

/**
 * Single source of truth for all shared validation rules (DRY).
 * Every layer (Model, Service, CLI) must use these rules instead of its own regular expressions.
 */
public final class Validators {
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_COMMENT_LENGTH = 500;
    public static final int MAX_SEARCH_LENGTH = 100;

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^09\\d{9}$");
    private static final Pattern POSTAL_CODE = Pattern.compile("^\\d{10}$");

    private Validators() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidPostalCode(String postalCode) {
        return postalCode != null && POSTAL_CODE.matcher(postalCode.trim()).matches();
    }

    /** Emails are compared case-insensitively; blank means "not provided" (null). */
    public static String normalizeEmail(String email) {
        return isBlank(email) ? null : email.trim().toLowerCase();
    }

    /** Blank means "not provided" (null). */
    public static String normalizePhone(String phone) {
        return isBlank(phone) ? null : phone.trim();
    }

    /** Returns the trimmed value or throws if it is null/blank. */
    public static String requireNonBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
        return value;
    }
}
