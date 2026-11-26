package shop.service;

import shop.model.Product;
import shop.model.StockMovement;
import shop.model.User;
import shop.repository.ProductRepository;

import java.util.Comparator;
import java.util.List;

/** Admin-only stock management (stories 28 and 29). Every change is logged in the product. */
public class InventoryService {
    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /** Warehouse view (story 28): every product, lowest stock first, so shortages are seen at once. */
    public List<Product> viewInventory(User actor) {
        Authorization.requireAdmin(actor);
        List<Product> inventory = productRepository.findAll();
        inventory.sort(Comparator.comparingInt(Product::getStock).thenComparing(Product::getTitle));
        return inventory;
    }

    public Product increaseStock(User actor, String productId, int amount, String reason) {
        Authorization.requireAdmin(actor);
        Product product = find(productId);
        product.increaseStock(amount, reason, actor.getId());
        return productRepository.save(product);
    }

    public Product decreaseStock(User actor, String productId, int amount, String reason) {
        Authorization.requireAdmin(actor);
        Product product = find(productId);
        product.decreaseStock(amount, reason, actor.getId());
        return productRepository.save(product);
    }

    public Product setStock(User actor, String productId, int newStock, String reason) {
        Authorization.requireAdmin(actor);
        Product product = find(productId);
        product.adjustStockTo(newStock, reason, actor.getId());
        return productRepository.save(product);
    }

    public List<StockMovement> getStockHistory(User actor, String productId) {
        Authorization.requireAdmin(actor);
        return find(productId).getStockHistory();
    }

    private Product find(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("No product with the specified ID was found."));
    }
}
