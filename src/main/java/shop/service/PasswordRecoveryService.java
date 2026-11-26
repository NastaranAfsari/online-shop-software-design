package shop.service;

import shop.model.PasswordResetToken;
import shop.model.User;
import shop.model.Validators;
import shop.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** Password recovery with a one-time code (simulated delivery of the code). */
public class PasswordRecoveryService {
    private static final int RESET_CODE_MINUTES = 10;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final Map<String, PasswordResetToken> resetTokens = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    public PasswordRecoveryService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Step 1: the identifier must belong to a registered user. A one-time code is generated.
     * In a real system it would be e-mailed/SMSed; here it is returned so the CLI can display it.
     */
    public String requestPasswordReset(String identifier) {
        if (Validators.isBlank(identifier)) {
            throw new IllegalArgumentException("The email or phone number cannot be empty.");
        }
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("No user with this email/phone was found."));
        String code = String.format("%06d", random.nextInt(1_000_000));
        resetTokens.put(user.getId(), new PasswordResetToken(code, LocalDateTime.now().plusMinutes(RESET_CODE_MINUTES)));
        return code;
    }

    /** Step 2: the code proves access to the account; then the new password is saved as a hash. */
    public void resetPassword(String identifier, String code, String newPassword) {
        if (!Validators.isValidPassword(newPassword)) {
            throw new IllegalArgumentException(
                    "The new password must be at least " + Validators.MIN_PASSWORD_LENGTH + " characters long.");
        }
        User user = userRepository.findByIdentifier(Validators.isBlank(identifier) ? "" : identifier)
                .orElseThrow(() -> new IllegalArgumentException("No user with this email/phone was found."));
        PasswordResetToken token = resetTokens.get(user.getId());
        if (token == null || token.isExpired() || !token.matches(code)) {
            throw new IllegalArgumentException("The recovery code is invalid or expired.");
        }
        user.changePasswordHash(passwordHasher.hash(newPassword));
        userRepository.save(user);
        resetTokens.remove(user.getId());
    }
}
