package shop.service;

import shop.model.Order;

import java.util.Optional;

/**
 * One check that a gateway result must pass before an order is marked as paid.
 * New checks are added as new classes without modifying PaymentService (OCP).
 * Rules are evaluated in order; a rule may assume that the rules before it have passed.
 */
public interface PaymentVerificationRule {
    /** Returns the reason for rejecting the payment, or empty if this check passes. */
    Optional<String> check(Order order, GatewayResponse response);
}
