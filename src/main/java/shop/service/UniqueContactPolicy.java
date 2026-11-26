package shop.service;

import shop.model.User;
import shop.repository.UserRepository;

/** Rule shared by registration and profile editing: an email or phone number belongs to one user only. */
final class UniqueContactPolicy {
    private final UserRepository userRepository;

    UniqueContactPolicy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Throws if another user (not {@code self}) already uses the email or the phone number. */
    void requireUnique(String email, String phone, User self) {
        if (email != null && userRepository.findByEmail(email).filter(u -> u != self).isPresent()) {
            throw new IllegalArgumentException("A user with this email has already registered.");
        }
        if (phone != null && userRepository.findByPhone(phone).filter(u -> u != self).isPresent()) {
            throw new IllegalArgumentException("A user with this phone number has already registered.");
        }
    }
}
