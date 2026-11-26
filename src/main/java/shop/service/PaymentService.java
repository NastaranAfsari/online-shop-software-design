package shop.service;

import shop.model.Order;
import shop.model.Payment;
import shop.model.PaymentMethod;
import shop.model.User;
import shop.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

/**
 * Payment and payment verification (stories 23-25).
 * The amount always comes from the order; the gateway result is verified before the order becomes PAID.
 */
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentGateway gateway;
    private final List<PaymentVerificationRule> verificationRules;
    private final OrderStockHandler stockHandler;

    public PaymentService(OrderRepository orderRepository, PaymentGateway gateway,
                          List<PaymentVerificationRule> verificationRules, OrderStockHandler stockHandler) {
        this.orderRepository = orderRepository;
        this.gateway = gateway;
        this.verificationRules = List.copyOf(verificationRules);
        this.stockHandler = stockHandler;
    }

    /**
     * Pays a PENDING order of the acting customer. Returns the finalized Payment (SUCCESS or FAILED).
     * A failed payment keeps the order PENDING so the customer can try again.
     */
    public Payment pay(User user, String orderId, PaymentMethod method) {
        Authorization.requireCustomer(user);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No order with this ID was found."));
        if (!order.getUserId().equals(user.getId())) {
            throw new SecurityException("This order does not belong to you.");
        }
        if (!order.isPayable()) {
            throw new IllegalStateException(order.getSuccessfulPayment().isPresent()
                    ? "This order has already been paid."
                    : "Only pending orders can be paid (current status: " + order.getStatus() + ").");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        Payment payment = new Payment(order.getId(), order.getTotalAmount(), method);
        GatewayResponse response = gateway.charge(order.getId(), payment.getAmount(), method);
        String failure = verify(order, response);
        if (failure == null) {
            payment.markAsSuccess(response.getTransactionRef());
        } else {
            payment.markAsFailed(failure);
        }

        order.registerPayment(payment);
        if (payment.isSuccessful()) {
            stockHandler.deductForPaidOrder(order, user.getId());
        }
        orderRepository.save(order);
        return payment;
    }

    public List<Payment> getPayments(User actor, String orderId) {
        Authorization.requireLoggedIn(actor);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No order with this ID was found."));
        if (!actor.isAdmin() && !order.getUserId().equals(actor.getId())) {
            throw new SecurityException("This order does not belong to you.");
        }
        return order.getPayments();
    }

    /** Runs the rules in order and returns the first rejection reason, or null if all pass. */
    private String verify(Order order, GatewayResponse response) {
        for (PaymentVerificationRule rule : verificationRules) {
            Optional<String> failure = rule.check(order, response);
            if (failure.isPresent()) {
                return failure.get();
            }
        }
        return null;
    }
}
