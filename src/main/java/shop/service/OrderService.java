package shop.service;

import shop.model.Address;
import shop.model.CartItem;
import shop.model.Order;
import shop.model.OrderStatus;
import shop.model.Product;
import shop.model.User;
import shop.repository.OrderRepository;

import java.util.List;

/** Order placement, order queries, cancellation and admin order management (stories 20-22, 26, 27, 36). */
public class OrderService implements PurchaseHistory {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderStockHandler stockHandler;

    public OrderService(OrderRepository orderRepository, CartService cartService, OrderStockHandler stockHandler) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.stockHandler = stockHandler;
    }

    /**
     * Creates a PENDING order from the cart. Stock is checked here but only deducted
     * after a confirmed payment (see PaymentService). The total is computed by the system.
     */
    public Order placeOrder(User user, String addressTitle) {
        Authorization.requireCustomer(user);
        if (user.getCart().isEmpty()) {
            throw new IllegalStateException("Your shopping cart is empty.");
        }
        Address address = user.findAddress(addressTitle)
                .orElseThrow(() -> new IllegalArgumentException("The selected address does not belong to you."));
        for (CartItem item : user.getCart().getItems()) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new IllegalStateException("The product '" + product.getTitle()
                        + "' is deactivated. Remove it from your cart first.");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalStateException("The product '" + product.getTitle() + "' does not have enough stock.");
            }
        }
        Order order = new Order(user.getId(), address);
        for (CartItem item : user.getCart().getItems()) {
            order.addItem(item.getProduct(), item.getQuantity());
        }
        orderRepository.save(order);
        cartService.clearCart(user);
        return order;
    }

    public List<Order> getUserOrders(User user) {
        Authorization.requireCustomer(user);
        return orderRepository.findByUserId(user.getId());
    }

    /** A customer sees only own orders; an admin sees any order. */
    public Order getOrderDetails(User actor, String orderId) {
        Authorization.requireLoggedIn(actor);
        Order order = findOrder(orderId);
        if (!actor.isAdmin() && !order.getUserId().equals(actor.getId())) {
            throw new SecurityException("This order does not belong to you.");
        }
        return order;
    }

    public List<Order> getAllOrders(User admin) {
        Authorization.requireAdmin(admin);
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(User admin, OrderStatus status) {
        Authorization.requireAdmin(admin);
        return orderRepository.findByStatus(status);
    }

    /** Admin status changes follow the life cycle. PAID can only be set by a confirmed payment. */
    public Order updateOrderStatus(User admin, String orderId, OrderStatus newStatus) {
        Authorization.requireAdmin(admin);
        Order order = findOrder(orderId);
        if (newStatus == OrderStatus.CANCELED) {
            return cancelAndRestock(order, admin);
        }
        order.changeStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Cancellation policy: a customer may cancel only own PENDING orders; an admin may cancel
     * PENDING or PAID orders. A paid order is refunded and its stock is returned.
     */
    public Order cancelOrder(User actor, String orderId) {
        Authorization.requireLoggedIn(actor);
        Order order = findOrder(orderId);
        if (!actor.isAdmin()) {
            if (!order.getUserId().equals(actor.getId())) {
                throw new SecurityException("This order does not belong to you.");
            }
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalStateException("Only pending orders can be canceled by the customer.");
            }
        }
        return cancelAndRestock(order, actor);
    }

    /** True if the user has a paid (PAID/SHIPPED/DELIVERED) order containing the product (stories 30, 31). */
    @Override
    public boolean hasUserPurchased(String userId, String productId) {
        return orderRepository.findByUserId(userId).stream()
                .anyMatch(o -> o.getStatus().isPaidState() && o.containsProduct(productId));
    }

    private Order cancelAndRestock(Order order, User actor) {
        boolean wasPaid = order.cancel();
        if (wasPaid) {
            stockHandler.restockCanceledOrder(order, actor.getId());
        }
        return orderRepository.save(order);
    }

    private Order findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No order with this ID was found."));
    }
}
