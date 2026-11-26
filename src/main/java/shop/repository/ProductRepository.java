package shop.repository;

import shop.model.Product;

import java.util.List;

public interface ProductRepository extends BaseRepository<Product> {
    List<Product> findByCategoryId(String categoryId);
}
