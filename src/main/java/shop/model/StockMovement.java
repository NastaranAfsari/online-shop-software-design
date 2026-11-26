package shop.model;

import java.time.LocalDateTime;

/** Immutable log entry: every stock change is recorded (story 29). */
public final class StockMovement {
    private final String productId;
    private final int delta;
    private final int resultingStock;
    private final String reason;
    private final String performedBy;
    private final LocalDateTime timestamp;

    public StockMovement(String productId, int delta, int resultingStock, String reason, String performedBy) {
        this.productId = productId;
        this.delta = delta;
        this.resultingStock = resultingStock;
        this.reason = Validators.isBlank(reason) ? "-" : reason.trim();
        this.performedBy = Validators.isBlank(performedBy) ? "system" : performedBy;
        this.timestamp = LocalDateTime.now();
    }

    public String getProductId() {
        return productId;
    }

    public int getDelta() {
        return delta;
    }

    public int getResultingStock() {
        return resultingStock;
    }

    public String getReason() {
        return reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
