package shop.repository;

import shop.model.User;
import shop.model.Validators;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new LinkedHashMap<>();

    @Override
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(String id) {
        users.remove(id);
    }

    /** Case-insensitive lookup (emails are stored normalized). */
    @Override
    public Optional<User> findByEmail(String email) {
        String normalized = Validators.normalizeEmail(email);
        if (normalized == null) {
            return Optional.empty();
        }
        return users.values().stream().filter(u -> normalized.equals(u.getEmail())).findFirst();
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        String normalized = Validators.normalizePhone(phone);
        if (normalized == null) {
            return Optional.empty();
        }
        return users.values().stream().filter(u -> normalized.equals(u.getPhone())).findFirst();
    }
}
