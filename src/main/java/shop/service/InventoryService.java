package shop.service;

import shop.model.Order;
import shop.model.OrderItem;
import shop.model.Product;
import shop.model.StockMovement;
import shop.model.User;
import shop.repository.ProductRepository;

import java.util.Comparator;
import java.util.List;

/**
 * Stock management: admin operations (stories 28 and 29) and the stock changes caused by orders.
 * Every change is logged in the product.
 */
public class InventoryService implements OrderStockHandler {
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

    @Override
    public void deductForPaidOrder(Order order, String performedBy) {
        for (OrderItem item : order.getItems()) {
            Product product = find(item.getProductId());
            product.decreaseStock(item.getQuantity(), "Order " + order.getId() + " paid", performedBy);
            product.recordSale(item.getQuantity());
            productRepository.save(product);
        }
    }

    @Override
    public void restockCanceledOrder(Order order, String performedBy) {
        for (OrderItem item : order.getItems()) {
            Product product = find(item.getProductId());
            product.increaseStock(item.getQuantity(), "Order " + order.getId() + " canceled", performedBy);
            product.revertSale(item.getQuantity());
            productRepository.save(product);
        }
    }

    private Product find(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("No product with the specified ID was found."));
    }
}
