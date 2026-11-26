package shop.service;

import shop.model.Order;
import shop.model.OrderItem;
import shop.model.Product;

import java.util.Optional;

/** Every ordered product must still have enough stock at the moment of payment. */
public class StockAvailabilityRule implements PaymentVerificationRule {
    private final ProductLookup productLookup;

    public StockAvailabilityRule(ProductLookup productLookup) {
        this.productLookup = productLookup;
    }

    @Override
    public Optional<String> check(Order order, GatewayResponse response) {
        for (OrderItem item : order.getItems()) {
            Product product = productLookup.getProductById(item.getProductId());
            if (product.getStock() < item.getQuantity()) {
                return Optional.of("Product '" + product.getTitle() + "' ran out of stock.");
            }
        }
        return Optional.empty();
    }
}
