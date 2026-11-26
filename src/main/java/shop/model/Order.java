package shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Aggregate root of an order: items, delivery address, payment attempts and status life cycle. */
public class Order {
    private final String id;
    private final String userId;
    private final List<OrderItem> items = new ArrayList<>();
    private final Address deliveryAddress;
    private final List<Payment> payments = new ArrayList<>();
    private final LocalDateTime creationDate;
    private OrderStatus status = OrderStatus.PENDING;

    public Order(String userId, Address deliveryAddress) {
        this.id = UUID.randomUUID().toString();
        this.userId = Validators.requireNonBlank(userId, "User id");
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("A delivery address is required.");
        }
        this.deliveryAddress = deliveryAddress;
        this.creationDate = LocalDateTime.now();
    }

    /** Items can only be added while the order is still pending. */
    public void addItem(Product product, int quantity) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Items can only be added to a pending order.");
        }
        items.add(new OrderItem(product.getId(), product.getTitle(), product.getPrice(), quantity));
    }

    /**
     * Records a finalized payment attempt. A successful attempt with the exact order amount
     * moves the order to PAID; a failed attempt keeps it PENDING so the customer can retry.
     */
    public void registerPayment(Payment payment) {
        if (!payment.getOrderId().equals(id)) {
            throw new IllegalArgumentException("This payment belongs to another order.");
        }
        if (status == OrderStatus.PAID || getSuccessfulPayment().isPresent()) {
            throw new IllegalStateException("This order has already been paid.");
        }
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be paid (current status: " + status + ").");
        }
        if (payment.getStatus() == PaymentStatus.PENDING) {
            throw new IllegalArgumentException("The payment result must be known before it is registered.");
        }
        if (payment.isSuccessful()) {
            if (payment.getAmount().compareTo(getTotalAmount()) != 0) {
                throw new IllegalArgumentException("The paid amount does not match the order amount.");
            }
            status = OrderStatus.PAID;
        }
        payments.add(payment);
    }

    /** Moves the order along the allowed life cycle. PAID is reachable only through registerPayment. */
    public void changeStatus(OrderStatus newStatus) {
        if (newStatus == OrderStatus.PAID) {
            throw new IllegalStateException("An order becomes PAID only through a confirmed payment.");
        }
        if (newStatus == OrderStatus.CANCELED) {
            cancel();
            return;
        }
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException("Invalid status change: " + status + " -> " + newStatus);
        }
        status = newStatus;
    }

    /**
     * Cancels a pending or paid order. A successful payment is refunded.
     *
     * @return true if the order had been paid (the caller must then return the stock)
     */
    public boolean cancel() {
        if (!status.canTransitionTo(OrderStatus.CANCELED)) {
            throw new IllegalStateException("An order with status " + status + " cannot be canceled.");
        }
        boolean wasPaid = status == OrderStatus.PAID;
        if (wasPaid) {
            getSuccessfulPayment().ifPresent(Payment::markAsRefunded);
        }
        status = OrderStatus.CANCELED;
        return wasPaid;
    }

    public boolean isPayable() {
        return status == OrderStatus.PENDING;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = Money.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    public Optional<Payment> getSuccessfulPayment() {
        return payments.stream().filter(Payment::isSuccessful).findFirst();
    }

    public Optional<Payment> getLatestPayment() {
        return payments.isEmpty() ? Optional.empty() : Optional.of(payments.get(payments.size() - 1));
    }

    public boolean containsProduct(String productId) {
        return items.stream().anyMatch(i -> i.getProductId().equals(productId));
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<Payment> getPayments() {
        return Collections.unmodifiableList(payments);
    }
}
