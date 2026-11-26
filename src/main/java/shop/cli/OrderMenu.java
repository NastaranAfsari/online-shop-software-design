package shop.cli;

import shop.model.Address;
import shop.model.Order;
import shop.model.Payment;
import shop.model.PaymentMethod;
import shop.model.User;
import shop.service.CartService;
import shop.service.OrderService;
import shop.service.PaymentService;
import shop.service.SimulatedPaymentGateway;
import shop.service.UserSession;

import java.util.List;

/** Checkout, payment and order history screens (stories 20-27). */
public class OrderMenu {
    private final ConsoleIO io;
    private final ConsolePrinter printer;
    private final UserSession session;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final SimulatedPaymentGateway gateway;

    public OrderMenu(ConsoleIO io, ConsolePrinter printer, UserSession session, CartService cartService,
                     OrderService orderService, PaymentService paymentService, SimulatedPaymentGateway gateway) {
        this.io = io;
        this.printer = printer;
        this.session = session;
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.gateway = gateway;
    }

    public void checkout() {
        User user = session.getCurrentUser();
        printer.printCart(cartService.getCart(user));
        if (user.getCart().isEmpty()) {
            return;
        }
        List<Address> addresses = user.getAddresses();
        if (addresses.isEmpty()) {
            io.println("You have no registered addresses. Please add an address first (Profile menu).");
            return;
        }
        io.println("\nSelect delivery address:");
        printer.printAddresses(addresses);
        int index = io.chooseIndex("Address", addresses.size());
        if (index < 0) {
            return;
        }
        Order order = orderService.placeOrder(user, addresses.get(index).getTitle());
        io.println("Order created. Order ID: " + order.getId());
        io.printf("Final amount to pay: $%.2f | Status: %s%n", order.getTotalAmount(), order.getStatus());
        if (io.confirm("Proceed to payment now?")) {
            processPayment(order.getId());
        } else {
            io.println("You can pay later from the menu (Pay pending order).");
        }
    }

    public void payPending() {
        String orderId = io.readLine("Enter Order ID to pay: ");
        processPayment(orderId);
    }

    private void processPayment(String orderId) {
        User user = session.getCurrentUser();
        Order order = orderService.getOrderDetails(user, orderId);
        if (!order.isPayable()) {
            throw new IllegalStateException(order.getStatus().isPaidState()
                    ? "This order has already been paid."
                    : "Only pending orders can be paid (current status: " + order.getStatus() + ").");
        }
        while (true) {
            io.printf("%n--- Payment Gateway --- Amount: $%.2f%n", order.getTotalAmount());
            io.println("1. Credit card");
            io.println("2. Online gateway");
            PaymentMethod method = io.readLine("Payment method: ").equals("1")
                    ? PaymentMethod.CREDIT_CARD : PaymentMethod.ONLINE_GATEWAY;
            gateway.setApproveNext(io.confirm("Simulate gateway success?"));
            Payment payment = paymentService.pay(user, orderId, method);
            if (payment.isSuccessful()) {
                io.println("Payment successful! Order status is now " + order.getStatus() + ".");
                return;
            }
            io.println("Payment FAILED: " + payment.getFailureReason() + " The order is still " + order.getStatus() + ".");
            if (!io.confirm("Try again?")) {
                io.println("You can pay later from the menu.");
                return;
            }
        }
    }

    public void viewOrders() {
        List<Order> orders = orderService.getUserOrders(session.getCurrentUser());
        if (orders.isEmpty()) {
            io.println("No orders found.");
            return;
        }
        orders.forEach(printer::printOrderLine);
    }

    public void viewOrderDetails() {
        String orderId = io.readLine("Enter Order ID: ");
        printer.printOrderDetails(orderService.getOrderDetails(session.getCurrentUser(), orderId), false);
    }

    public void cancelPending() {
        String orderId = io.readLine("Enter Order ID to cancel: ");
        if (io.confirm("Cancel this order?")) {
            orderService.cancelOrder(session.getCurrentUser(), orderId);
            io.println("Order canceled.");
        }
    }
}
