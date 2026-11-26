package shop.cli;

import shop.service.AuthService;
import shop.service.CartService;

/** Cart screens (stories 15-19). */
public class CartMenu {
    private final ConsoleIO io;
    private final ConsolePrinter printer;
    private final CartService cartService;
    private final AuthService authService;

    public CartMenu(ConsoleIO io, ConsolePrinter printer, CartService cartService, AuthService authService) {
        this.io = io;
        this.printer = printer;
        this.cartService = cartService;
        this.authService = authService;
    }

    public void view() {
        printer.printCart(cartService.getCart(authService.getCurrentUser()));
    }

    public void add() {
        String productId = io.readLine("Enter Product ID: ");
        int quantity = io.readInt("Enter quantity: ");
        cartService.addToCart(authService.getCurrentUser(), productId, quantity);
        io.println("Added to cart successfully.");
    }

    public void changeQuantity() {
        String productId = io.readLine("Enter Product ID: ");
        int quantity = io.readInt("New quantity (minimum 1): ");
        cartService.updateQuantity(authService.getCurrentUser(), productId, quantity);
        io.println("Quantity updated.");
    }

    public void remove() {
        String productId = io.readLine("Enter Product ID: ");
        io.println("1. Decrease quantity by 1");
        io.println("2. Remove completely from cart");
        String choice = io.readLine("Choose an option: ");
        if (choice.equals("1")) {
            cartService.decreaseQuantity(authService.getCurrentUser(), productId, 1);
            io.println("Quantity decreased successfully.");
        } else if (choice.equals("2")) {
            cartService.removeFromCart(authService.getCurrentUser(), productId);
            io.println("Product removed from cart.");
        } else {
            io.println("Invalid option.");
        }
    }

    public void clear() {
        if (io.confirm("Remove all products from the cart?")) {
            cartService.clearCart(authService.getCurrentUser());
            io.println("Cart is now empty.");
        }
    }
}
