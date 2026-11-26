package shop.repository;

import shop.model.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
}
