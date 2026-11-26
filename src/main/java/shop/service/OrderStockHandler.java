package shop.service;

import shop.model.Order;

/**
 * Stock changes caused by orders. Payment and order services depend only on this small
 * interface, not on the admin inventory operations (ISP/DIP). Implemented by InventoryService.
 */
public interface OrderStockHandler {
    /** After a confirmed payment: removes the ordered units from stock and records the sale. */
    void deductForPaidOrder(Order order, String performedBy);

    /** After a paid order is canceled: returns the units to stock and reverts the sale. */
    void restockCanceledOrder(Order order, String performedBy);
}
