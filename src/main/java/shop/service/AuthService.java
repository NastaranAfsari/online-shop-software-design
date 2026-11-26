package shop.service;

import shop.model.Role;
import shop.model.User;
import shop.model.Validators;
import shop.repository.UserRepository;

/** Registration and login/logout (account access). */
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UserSession session;
    private final UniqueContactPolicy uniqueContactPolicy;

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
        User user = userRepository.findByIdentifier(identifier)
                .filter(u -> passwordHasher.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Incorrect email/phone or password."));
        session.start(user);
        return user;
    }

    public void logout() {
        session.end();
    }
}
