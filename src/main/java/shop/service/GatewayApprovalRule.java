package shop.service;

import shop.model.Order;

import java.util.Optional;

/** The gateway must have answered and approved the payment. */
public class GatewayApprovalRule implements PaymentVerificationRule {
    @Override
    public Optional<String> check(Order order, GatewayResponse response) {
        if (response == null || !response.isApproved()) {
            return Optional.of(response == null || response.getMessage() == null
                    ? "Payment was not approved." : response.getMessage());
        }
        return Optional.empty();
    }
}
