package shop.cli;

import shop.model.User;
import shop.service.UserSession;

import java.util.NoSuchElementException;

/** Entry point of the presentation layer: shows the right menu for guest, customer or admin. */
public class ConsoleApp {
    private final ConsoleIO io;
    private final UserSession session;
    private final AuthMenu authMenu;
    private final ProfileMenu profileMenu;
    private final ProductMenu productMenu;
    private final CartMenu cartMenu;
    private final OrderMenu orderMenu;
    private final AdminMenu adminMenu;
    private boolean running = true;

    public ConsoleApp(ConsoleIO io, UserSession session, AuthMenu authMenu, ProfileMenu profileMenu,
                      ProductMenu productMenu, CartMenu cartMenu, OrderMenu orderMenu, AdminMenu adminMenu) {
        this.io = io;
        this.session = session;
        this.authMenu = authMenu;
        this.profileMenu = profileMenu;
        this.productMenu = productMenu;
        this.cartMenu = cartMenu;
        this.orderMenu = orderMenu;
        this.adminMenu = adminMenu;
    }

    public void start() {
        try {
            while (running) {
                if (!session.isLoggedIn()) {
                    showGuestMenu();
                } else if (session.isAdmin()) {
                    showAdminMenu();
                } else {
                    showCustomerMenu();
                }
            }
        } catch (NoSuchElementException endOfInput) {
            io.println("\nInput closed. Goodbye!");
        }
    }

    private void showGuestMenu() {
        io.println("\n=== Welcome to Online Shop ===");
        io.println("1. Login");
        io.println("2. Register");
        io.println("3. Browse products");
        io.println("4. Search products");
        io.println("5. Filter & sort products");
        io.println("6. Product details");
        io.println("7. Forgot password");
        io.println("0. Exit");
        String choice = io.readLine("Select an option: ");
        io.runSafely(() -> {
            switch (choice) {
                case "1": authMenu.login(); break;
                case "2": authMenu.register(); break;
                case "3": productMenu.browse(); break;
                case "4": productMenu.search(); break;
                case "5": productMenu.filterAndSort(); break;
                case "6": productMenu.showDetails(); break;
                case "7": authMenu.forgotPassword(); break;
                case "0":
                    io.println("Goodbye!");
                    running = false;
                    break;
                default: io.println("Invalid option. Please try again.");
            }
        });
    }

    private void showCustomerMenu() {
        User user = session.getCurrentUser();
        io.println("\n=== Customer Menu (" + user.getFullName() + ") ===");
        io.println("1. Browse products");
        io.println("2. Search products");
        io.println("3. Filter & sort products");
        io.println("4. Product details");
        io.println("5. View cart");
        io.println("6. Add product to cart");
        io.println("7. Change product quantity in cart");
        io.println("8. Remove product from cart");
        io.println("9. Clear cart");
        io.println("10. Checkout / place order");
        io.println("11. Pay pending order");
        io.println("12. My orders");
        io.println("13. Order details");
        io.println("14. Cancel pending order");
        io.println("15. Rate a purchased product");
        io.println("16. Comment on a purchased product");
        io.println("17. My profile");
        io.println("18. Edit profile / manage addresses");
        io.println("19. Logout");
        String choice = io.readLine("Select an option: ");
        io.runSafely(() -> {
            switch (choice) {
                case "1": productMenu.browse(); break;
                case "2": productMenu.search(); break;
                case "3": productMenu.filterAndSort(); break;
                case "4": productMenu.showDetails(); break;
                case "5": cartMenu.view(); break;
                case "6": cartMenu.add(); break;
                case "7": cartMenu.changeQuantity(); break;
                case "8": cartMenu.remove(); break;
                case "9": cartMenu.clear(); break;
                case "10": orderMenu.checkout(); break;
                case "11": orderMenu.payPending(); break;
                case "12": orderMenu.viewOrders(); break;
                case "13": orderMenu.viewOrderDetails(); break;
                case "14": orderMenu.cancelPending(); break;
                case "15": productMenu.rate(); break;
                case "16": productMenu.comment(); break;
                case "17": profileMenu.viewProfile(); break;
                case "18": profileMenu.manage(); break;
                case "19": authMenu.logout(); break;
                default: io.println("Invalid option.");
            }
        });
    }

    private void showAdminMenu() {
        User user = session.getCurrentUser();
        io.println("\n=== Admin Panel (" + user.getFullName() + ") ===");
        io.println("1. Add category");
        io.println("2. Enable/disable category");
        io.println("3. Add product");
        io.println("4. Edit product");
        io.println("5. Enable/disable product");
        io.println("6. View all products (catalog: price, brand, rating, active/inactive)");
        io.println("7. View inventory (warehouse: stock, sold units, stock state)");
        io.println("8. Change stock / stock history");
        io.println("9. View orders (filter by status)");
        io.println("10. Order details");
        io.println("11. Update order status");
        io.println("12. Logout");
        String choice = io.readLine("Select an option: ");
        io.runSafely(() -> {
            switch (choice) {
                case "1": adminMenu.addCategory(); break;
                case "2": adminMenu.toggleCategory(); break;
                case "3": adminMenu.addProduct(); break;
                case "4": adminMenu.editProduct(); break;
                case "5": adminMenu.toggleProduct(); break;
                case "6": adminMenu.viewAllProducts(); break;
                case "7": adminMenu.viewInventory(); break;
                case "8": adminMenu.changeStock(); break;
                case "9": adminMenu.viewOrders(); break;
                case "10": adminMenu.viewOrderDetails(); break;
                case "11": adminMenu.updateOrderStatus(); break;
                case "12": authMenu.logout(); break;
                default: io.println("Invalid option.");
            }
        });
    }
}
