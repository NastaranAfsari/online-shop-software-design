package shop.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Shopping cart of one user. All quantity/stock rules are enforced here. */
public class Cart {
    private final String userId;
    private final List<CartItem> items = new ArrayList<>();

    public Cart(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Optional<CartItem> findItem(String productId) {
        return items.stream().filter(i -> i.getProduct().getId().equals(productId)).findFirst();
    }

    /** Adds the product; if it is already in the cart the quantity increases. */
    public void addItem(Product product, int quantity) {
        Validators.requirePositive(quantity, "Quantity");
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (!product.isActive()) {
            throw new IllegalStateException("This product is deactivated and cannot be added to the cart.");
        }
        Optional<CartItem> existing = findItem(product.getId());
        int newTotal = quantity + existing.map(CartItem::getQuantity).orElse(0);
        requireEnoughStock(product, newTotal);
        if (existing.isPresent()) {
            existing.get().setQuantity(newTotal);
        } else {
            items.add(new CartItem(product, quantity));
        }
    }

    /** Sets an exact quantity (minimum 1). */
    public void updateQuantity(String productId, int newQuantity) {
        CartItem item = requireItem(productId);
        Validators.requirePositive(newQuantity, "Quantity");
        if (!item.getProduct().isActive()) {
            throw new IllegalStateException("This product is deactivated and cannot be modified.");
        }
        requireEnoughStock(item.getProduct(), newQuantity);
        item.setQuantity(newQuantity);
    }

    /** Decreases the quantity; when it would reach zero the item is removed. */
    public void decreaseItem(String productId, int amount) {
        Validators.requirePositive(amount, "Amount");
        CartItem item = requireItem(productId);
        int remaining = item.getQuantity() - amount;
        if (remaining > 0) {
            item.setQuantity(remaining);
        } else {
            items.remove(item);
        }
    }

    public void removeItem(String productId) {
        items.remove(requireItem(productId));
    }

    public void clear() {
        items.clear();
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getTotalPrice() {
        BigDecimal total = Money.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    private CartItem requireItem(String productId) {
        return findItem(productId)
                .orElseThrow(() -> new IllegalArgumentException("This product is not in the cart."));
    }

    private static void requireEnoughStock(Product product, int wantedQuantity) {
        if (wantedQuantity > product.getStock()) {
            throw new IllegalArgumentException("Insufficient stock! Current stock: " + product.getStock());
        }
    }
}
