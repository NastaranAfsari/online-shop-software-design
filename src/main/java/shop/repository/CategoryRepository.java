package shop.repository;

import shop.model.Category;

import java.util.Optional;

public interface CategoryRepository extends BaseRepository<Category> {
    Optional<Category> findByName(String name);
}
