package shop.cli;

import shop.model.Address;
import shop.model.Cart;
import shop.model.CartItem;
import shop.model.Order;
import shop.model.OrderItem;
import shop.model.Payment;
import shop.model.Product;
import shop.model.Review;
import shop.model.User;

import java.util.List;

/** Formats domain objects for the terminal. */
public class ConsolePrinter {
    private static final String LINE = "-".repeat(110);
    private final ConsoleIO io;

    public ConsolePrinter(ConsoleIO io) {
        this.io = io;
    }

    public void printProducts(List<Product> products) {
        if (products.isEmpty()) {
            io.println("No products found.");
            return;
        }
        io.println("\n" + LINE);
        io.printf("%-36s | %-18s | %-10s | %-10s | %-6s | %-12s%n", "ID", "Title", "Brand", "Price ($)", "Stock", "Rating");
        io.println(LINE);
        for (Product p : products) {
            String title = p.isActive() ? p.getTitle() : "[OFF] " + p.getTitle();
            String rating = p.getRatingCount() == 0 ? "-" : String.format("%.1f (%d)", p.getAverageRating(), p.getRatingCount());
            io.printf("%-36s | %-18s | %-10s | %-10.2f | %-6d | %-12s%n",
                    p.getId(), shorten(title, 18), shorten(p.getBrand(), 10), p.getPrice(), p.getStock(), rating);
        }
        io.println(LINE);
    }

    /** Warehouse table for admins (story 28): stock-oriented columns, not catalog columns. */
    public void printInventory(List<Product> products) {
        if (products.isEmpty()) {
            io.println("The inventory is empty.");
            return;
        }
        int totalUnits = 0;
        int outOfStock = 0;
        io.println("\n" + LINE);
        io.printf("%-36s | %-18s | %-12s | %-6s | %-6s | %-8s | %-12s%n",
                "ID", "Title", "Category", "Stock", "Sold", "Active", "Stock state");
        io.println(LINE);
        for (Product p : products) {
            totalUnits += p.getStock();
            if (p.getStock() == 0) {
                outOfStock++;
            }
            io.printf("%-36s | %-18s | %-12s | %-6d | %-6d | %-8s | %-12s%n", p.getId(), shorten(p.getTitle(), 18),
                    shorten(p.getCategory().getName(), 12), p.getStock(), p.getSoldCount(),
                    p.isActive() ? "Yes" : "No", p.getStock() == 0 ? "OUT OF STOCK" : "In stock");
        }
        io.println(LINE);
        io.printf("Products: %d | Units in stock: %d | Out of stock: %d%n", products.size(), totalUnits, outOfStock);
    }

    public void printProductDetails(Product p, List<Review> comments) {
        io.println("\n=== " + p.getTitle() + " ===");
        io.println("ID:          " + p.getId());
        io.println("Category:    " + p.getCategory().getName());
        io.println("Brand:       " + p.getBrand());
        io.printf("Price:       $%.2f%n", p.getPrice());
        io.println("Availability: " + (p.isAvailable() ? "In stock (" + p.getStock() + ")"
                : p.isActive() ? "Out of stock" : "Not available for purchase"));
        io.println("Rating:      " + (p.getRatingCount() == 0 ? "No ratings yet"
                : String.format("%.1f / 5 (%d ratings)", p.getAverageRating(), p.getRatingCount())));
        io.println("Description: " + (p.getDescription().isEmpty() ? "-" : p.getDescription()));
        io.println("Images:      " + (p.getImageUrls().isEmpty() ? "-" : String.join(", ", p.getImageUrls())));
        io.println("Comments:");
        if (comments.isEmpty()) {
            io.println("  (No comments yet)");
        }
        for (Review r : comments) {
            String score = r.hasScore() ? "[" + r.getScore() + "/5] " : "";
            io.println("  * " + score + r.getReviewerName() + ": " + r.getComment());
        }
    }

    public void printCart(Cart cart) {
        if (cart.isEmpty()) {
            io.println("Your cart is empty.");
            return;
        }
        io.println("\n--- Shopping Cart ---");
        for (CartItem item : cart.getItems()) {
            io.printf("%s | ID: %s | Unit: $%.2f | Qty: %d | Subtotal: $%.2f%n", item.getProduct().getTitle(),
                    item.getProduct().getId(), item.getProduct().getPrice(), item.getQuantity(), item.getSubtotal());
        }
        io.printf("Total: $%.2f (%d items)%n", cart.getTotalPrice(), cart.getTotalQuantity());
    }

    public void printOrderLine(Order o) {
        io.printf("Order %s | %s | Total: $%.2f | %s%n", o.getId(), o.getStatus(), o.getTotalAmount(),
                o.getCreationDate().toLocalDate());
    }

    public void printOrderDetails(Order o, boolean adminView) {
        io.println("\n=== Order " + o.getId() + " ===");
        io.println("Status:  " + o.getStatus());
        io.println("Date:    " + o.getCreationDate());
        if (adminView) {
            io.println("User ID: " + o.getUserId());
        }
        io.println("Address: " + o.getDeliveryAddress());
        for (OrderItem item : o.getItems()) {
            io.printf("  - %s x%d @ $%.2f = $%.2f%n", item.getProductName(), item.getQuantity(),
                    item.getUnitPrice(), item.getTotalPrice());
        }
        io.printf("Total:   $%.2f%n", o.getTotalAmount());
        for (Payment p : o.getPayments()) {
            String ref = adminView ? p.getMaskedTransactionRef() : String.valueOf(p.getTransactionRef());
            io.println("  Payment: " + p.getStatus() + " | " + p.getMethod() + " | ref " + ref
                    + (p.getFailureReason() != null ? " | " + p.getFailureReason() : ""));
        }
    }

    public void printAddresses(List<Address> addresses) {
        if (addresses.isEmpty()) {
            io.println("  (No addresses registered yet)");
            return;
        }
        for (int i = 0; i < addresses.size(); i++) {
            io.println("  " + (i + 1) + ". " + addresses.get(i));
        }
    }

    /** The password (hash) is never printed (story 5). */
    public void printProfile(User user) {
        io.println("\n=== My Profile ===");
        io.println("Full Name: " + user.getFullName());
        io.println("Email: " + (user.getEmail() != null ? user.getEmail() : "Not specified"));
        io.println("Phone: " + (user.getPhone() != null ? user.getPhone() : "Not specified"));
        io.println("Role: " + user.getRole());
        io.println("Addresses:");
        printAddresses(user.getAddresses());
    }

    private static String shorten(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + "~";
    }
}
