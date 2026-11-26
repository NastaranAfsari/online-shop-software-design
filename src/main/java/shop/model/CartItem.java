package shop.model;

import java.math.BigDecimal;

/** A product with a quantity inside a cart. Its quantity can be changed only through the Cart. */
public class CartItem {
    private final Product product;
    private int quantity;

    CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = Validators.requirePositive(quantity, "Quantity");
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = Validators.requirePositive(quantity, "Quantity");
    }

    /** Subtotal always uses the current price from the product, never a price given by the user. */
    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
