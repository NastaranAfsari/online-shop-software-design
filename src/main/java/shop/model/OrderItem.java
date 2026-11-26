package shop.model;

import java.math.BigDecimal;

/** Value Object: a snapshot of a product (name and price) at the time the order was placed. */
public final class OrderItem {
    private final String productId;
    private final String productName;
    private final BigDecimal unitPrice;
    private final int quantity;

    public OrderItem(String productId, String productName, BigDecimal unitPrice, int quantity) {
        this.productId = Validators.requireNonBlank(productId, "Product id");
        this.productName = Validators.requireNonBlank(productName, "Product name");
        this.unitPrice = Money.normalize(unitPrice, "Unit price");
        this.quantity = Validators.requirePositive(quantity, "Quantity");
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
