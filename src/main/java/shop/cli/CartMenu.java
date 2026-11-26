package shop.cli;

import shop.service.CartService;
import shop.service.UserSession;

/** Cart screens (stories 15-19). */
public class CartMenu {
    private final ConsoleIO io;
    private final ConsolePrinter printer;
    private final CartService cartService;
    private final UserSession session;

    public CartMenu(ConsoleIO io, ConsolePrinter printer, CartService cartService, UserSession session) {
        this.io = io;
        this.printer = printer;
        this.cartService = cartService;
        this.session = session;
    }

    public void view() {
        printer.printCart(cartService.getCart(session.getCurrentUser()));
    }

    public void add() {
        String productId = io.readLine("Enter Product ID: ");
        int quantity = io.readInt("Enter quantity: ");
        cartService.addToCart(session.getCurrentUser(), productId, quantity);
        io.println("Added to cart successfully.");
    }

    public void changeQuantity() {
        String productId = io.readLine("Enter Product ID: ");
        int quantity = io.readInt("New quantity (minimum 1): ");
        cartService.updateQuantity(session.getCurrentUser(), productId, quantity);
        io.println("Quantity updated.");
    }

    public void remove() {
        String productId = io.readLine("Enter Product ID: ");
        io.println("1. Decrease quantity by 1");
        io.println("2. Remove completely from cart");
        String choice = io.readLine("Choose an option: ");
        if (choice.equals("1")) {
            cartService.decreaseQuantity(session.getCurrentUser(), productId, 1);
            io.println("Quantity decreased successfully.");
        } else if (choice.equals("2")) {
            cartService.removeFromCart(session.getCurrentUser(), productId);
            io.println("Product removed from cart.");
        } else {
            io.println("Invalid option.");
        }
    }

    public void clear() {
        if (io.confirm("Remove all products from the cart?")) {
            cartService.clearCart(session.getCurrentUser());
            io.println("Cart is now empty.");
        }
    }
}
