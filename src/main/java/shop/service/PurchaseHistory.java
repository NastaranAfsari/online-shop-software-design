package shop.service;

/**
 * Narrow question about past orders, used by ReviewService (ISP).
 * Implemented by OrderService.
 */
public interface PurchaseHistory {
    /** True if the user has a paid (PAID/SHIPPED/DELIVERED) order containing the product. */
    boolean hasUserPurchased(String userId, String productId);
}
