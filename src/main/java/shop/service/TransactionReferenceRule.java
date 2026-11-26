package shop.service;

import shop.model.Order;
import shop.model.Validators;

import java.util.Optional;

/** An approved payment must come with a transaction reference from the gateway. */
public class TransactionReferenceRule implements PaymentVerificationRule {
    @Override
    public Optional<String> check(Order order, GatewayResponse response) {
        if (Validators.isBlank(response.getTransactionRef())) {
            return Optional.of("The gateway did not return a transaction reference.");
        }
        return Optional.empty();
    }
}
