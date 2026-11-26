package shop.service;

import shop.model.Order;
import shop.model.OrderItem;
import shop.model.Payment;
import shop.model.PaymentMethod;
import shop.model.Product;
import shop.model.User;
import shop.model.Validators;
import shop.repository.OrderRepository;

import java.util.List;

/**
 * Payment and payment verification (stories 23-25).
 * The amount always comes from the order; the gateway result is verified before the order becomes PAID.
 */
public class PaymentService {
    private final OrderRepository orderRepository;
    private final ProductLookup productLookup;
    private final PaymentGateway gateway;

    public PaymentService(OrderRepository orderRepository, ProductLookup productLookup, PaymentGateway gateway) {
        this.orderRepository = orderRepository;
        this.productLookup = productLookup;
        this.gateway = gateway;
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
            deductStock(order, user);
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

    /** Returns null when the gateway result is acceptable, otherwise the reason for rejecting it. */
    private String verify(Order order, GatewayResponse response) {
        if (response == null || !response.isApproved()) {
            return response == null || response.getMessage() == null ? "Payment was not approved." : response.getMessage();
        }
        if (response.getPaidAmount() == null || response.getPaidAmount().compareTo(order.getTotalAmount()) != 0) {
            return "The paid amount does not match the order amount.";
        }
        if (Validators.isBlank(response.getTransactionRef())) {
            return "The gateway did not return a transaction reference.";
        }
        for (OrderItem item : order.getItems()) {
            Product product = productLookup.getProductById(item.getProductId());
            if (product.getStock() < item.getQuantity()) {
                return "Product '" + product.getTitle() + "' ran out of stock.";
            }
        }
        return null;
    }

    private void deductStock(Order order, User user) {
        for (OrderItem item : order.getItems()) {
            Product product = productLookup.getProductById(item.getProductId());
            product.decreaseStock(item.getQuantity(), "Order " + order.getId() + " paid", user.getId());
            product.recordSale(item.getQuantity());
        }
    }
}
