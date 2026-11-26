package shop.model;

import java.time.LocalDateTime;

/** Value Object: a one-time recovery code with an expiry time (simulates the code sent by e-mail/SMS). */
public final class PasswordResetToken {
    private final String code;
    private final LocalDateTime expiresAt;

    public PasswordResetToken(String code, LocalDateTime expiresAt) {
        this.code = Validators.requireNonBlank(code, "Reset code");
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matches(String candidate) {
        return candidate != null && code.equals(candidate.trim());
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
