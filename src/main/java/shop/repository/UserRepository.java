package shop.repository;

import shop.model.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    /** Finds a user by an identifier that is either an email (contains '@') or a phone number. */
    default Optional<User> findByIdentifier(String identifier) {
        if (identifier == null) {
            return Optional.empty();
        }
        String trimmed = identifier.trim();
        return trimmed.contains("@") ? findByEmail(trimmed) : findByPhone(trimmed);
    }
}
