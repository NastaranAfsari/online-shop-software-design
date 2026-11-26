package shop.service;

import shop.model.Order;

import java.util.Optional;

/** The amount reported by the gateway must equal the order total calculated by the system. */
public class PaidAmountRule implements PaymentVerificationRule {
    @Override
    public Optional<String> check(Order order, GatewayResponse response) {
        if (response.getPaidAmount() == null || response.getPaidAmount().compareTo(order.getTotalAmount()) != 0) {
            return Optional.of("The paid amount does not match the order amount.");
        }
        return Optional.empty();
    }
}
