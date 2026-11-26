package shop.repository;

import java.util.List;
import java.util.Optional;

/** Generic storage contract. Implementations keep data in memory (no external database). */
public interface BaseRepository<T> {
    T save(T entity);

    Optional<T> findById(String id);

    List<T> findAll();

    void deleteById(String id);
}
