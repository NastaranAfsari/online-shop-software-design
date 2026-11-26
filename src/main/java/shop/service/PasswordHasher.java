package shop.service;

/**
 * Abstraction for password hashing. Services depend on this interface, so the hashing
 * algorithm can be replaced without changing AuthService or PasswordRecoveryService (DIP).
 */
public interface PasswordHasher {
    /** Returns a salted hash of the raw password (the raw password is never stored). */
    String hash(String rawPassword);

    /** True if the raw password produces the stored hash. */
    boolean matches(String rawPassword, String storedHash);
}
