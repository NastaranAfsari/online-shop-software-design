package shop.service;

import java.math.BigDecimal;

/** The result reported by the payment gateway; the shop verifies it before trusting it. */
public final class GatewayResponse {
    private final boolean approved;
    private final BigDecimal paidAmount;
    private final String transactionRef;
    private final String message;

    public GatewayResponse(boolean approved, BigDecimal paidAmount, String transactionRef, String message) {
        this.approved = approved;
        this.paidAmount = paidAmount;
        this.transactionRef = transactionRef;
        this.message = message;
    }

    public boolean isApproved() {
        return approved;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public String getMessage() {
        return message;
    }
}
