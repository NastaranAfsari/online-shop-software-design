package shop.service;

import shop.model.PasswordResetToken;
import shop.model.Role;
import shop.model.User;
import shop.model.Validators;
import shop.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Registration, login/logout and password recovery (stories 1-4). */
public class AuthService {
    private static final int RESET_CODE_MINUTES = 10;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UserSession session;
    private final UniqueContactPolicy uniqueContactPolicy;
    private final Map<String, PasswordResetToken> resetTokens = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher, UserSession session) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.session = session;
        this.uniqueContactPolicy = new UniqueContactPolicy(userRepository);
    }

    // ---------- registration ----------

    /** Public registration: the role is ALWAYS CUSTOMER; it cannot be chosen by the caller. */
    public User register(String fullName, String email, String phone, String password) {
        return createUser(fullName, email, phone, password, Role.CUSTOMER);
    }

    /** Bootstrap used once by Main: allowed only while the system has no admin yet. */
    public User registerFirstAdmin(String fullName, String email, String phone, String password) {
        boolean adminExists = userRepository.findAll().stream().anyMatch(User::isAdmin);
        if (adminExists) {
            throw new SecurityException("An admin already exists. New admins must be created by an admin.");
        }
        return createUser(fullName, email, phone, password, Role.ADMIN);
    }

    public User createAdmin(User actor, String fullName, String email, String phone, String password) {
        Authorization.requireAdmin(actor);
        return createUser(fullName, email, phone, password, Role.ADMIN);
    }

    private User createUser(String fullName, String email, String phone, String password, Role role) {
        String normalizedEmail = Validators.normalizeEmail(email);
        String normalizedPhone = Validators.normalizePhone(phone);
        if (normalizedEmail == null && normalizedPhone == null) {
            throw new IllegalArgumentException("An email or a phone number is required.");
        }
        if (!Validators.isValidPassword(password)) {
            throw new IllegalArgumentException(
                    "Password must be at least " + Validators.MIN_PASSWORD_LENGTH + " characters long.");
        }
        User user = new User(fullName, normalizedEmail, normalizedPhone, passwordHasher.hash(password), role);
        uniqueContactPolicy.requireUnique(user.getEmail(), user.getPhone(), null);
        return userRepository.save(user);
    }

    // ---------- session ----------

    /** Logs in with email or phone number. The same message is used for every failure. */
    public User login(String identifier, String password) {
        if (Validators.isBlank(identifier) || Validators.isBlank(password)) {
            throw new IllegalArgumentException("Email/phone and password cannot be empty.");
        }
        User user = findByIdentifier(identifier)
                .filter(u -> passwordHasher.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Incorrect email/phone or password."));
        session.start(user);
        return user;
    }

    public void logout() {
        session.end();
    }

    private Optional<User> findByIdentifier(String identifier) {
        String trimmed = identifier.trim();
        return trimmed.contains("@") ? userRepository.findByEmail(trimmed) : userRepository.findByPhone(trimmed);
    }

    // ---------- password recovery (simulated delivery of the code) ----------

    /**
     * Step 1: the identifier must belong to a registered user. A one-time code is generated.
     * In a real system it would be e-mailed/SMSed; here it is returned so the CLI can display it.
     */
    public String requestPasswordReset(String identifier) {
        if (Validators.isBlank(identifier)) {
            throw new IllegalArgumentException("The email or phone number cannot be empty.");
        }
        User user = findByIdentifier(identifier)
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
        User user = findByIdentifier(Validators.isBlank(identifier) ? "" : identifier)
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
