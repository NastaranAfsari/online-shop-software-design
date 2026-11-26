package shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** One payment attempt for an order. A new attempt creates a new Payment, so history is kept. */
public class Payment {
    private final String id;
    private final String orderId;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private PaymentStatus status = PaymentStatus.PENDING;
    private String transactionRef;
    private String failureReason;
    private LocalDateTime paymentDate;

    public Payment(String orderId, BigDecimal amount, PaymentMethod method) {
        this.id = UUID.randomUUID().toString();
        this.orderId = Validators.requireNonBlank(orderId, "Order id");
        this.amount = Money.normalize(amount, "Payment amount");
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }
        this.method = method;
    }

    public void markAsSuccess(String transactionRef) {
        requirePending();
        this.transactionRef = Validators.requireNonBlank(transactionRef, "Transaction reference");
        this.status = PaymentStatus.SUCCESS;
        this.paymentDate = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        requirePending();
        this.failureReason = Validators.isBlank(reason) ? "Payment failed." : reason.trim();
        this.status = PaymentStatus.FAILED;
        this.paymentDate = LocalDateTime.now();
    }

    public void markAsRefunded() {
        if (status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only a successful payment can be refunded.");
        }
        this.status = PaymentStatus.REFUNDED;
    }

    private void requirePending() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("This payment has already been finalized (" + status + ").");
        }
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }

    /** Sensitive data: only the last 4 characters of the reference are shown to admins. */
    public String getMaskedTransactionRef() {
        if (transactionRef == null) {
            return "-";
        }
        int visible = Math.min(4, transactionRef.length());
        return "****" + transactionRef.substring(transactionRef.length() - visible);
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
}
