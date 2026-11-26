package shop.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Order life cycle. Allowed transitions:
 * PENDING -> PAID | CANCELED, PAID -> SHIPPED | CANCELED, SHIPPED -> DELIVERED.
 * DELIVERED and CANCELED are final.
 */
public enum OrderStatus {
    PENDING, PAID, SHIPPED, DELIVERED, CANCELED;

    public Set<OrderStatus> getAllowedNextStatuses() {
        switch (this) {
            case PENDING:
                return EnumSet.of(PAID, CANCELED);
            case PAID:
                return EnumSet.of(SHIPPED, CANCELED);
            case SHIPPED:
                return EnumSet.of(DELIVERED);
            default:
                return EnumSet.noneOf(OrderStatus.class);
        }
    }

    public boolean canTransitionTo(OrderStatus next) {
        return getAllowedNextStatuses().contains(next);
    }

    /** True if the customer has really paid for the order (and it is not canceled). */
    public boolean isPaidState() {
        return this == PAID || this == SHIPPED || this == DELIVERED;
    }
}
