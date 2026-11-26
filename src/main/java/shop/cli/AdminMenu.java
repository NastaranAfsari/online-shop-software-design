package shop.cli;

import shop.model.Category;
import shop.model.Order;
import shop.model.OrderStatus;
import shop.model.Product;
import shop.model.StockMovement;
import shop.model.User;
import shop.service.InventoryService;
import shop.service.OrderService;
import shop.service.ProductService;
import shop.service.UserSession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Admin panel screens (stories 28, 29, 33-36). All permission checks are done again in the services. */
public class AdminMenu {
    private final ConsoleIO io;
    private final ConsolePrinter printer;
    private final UserSession session;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    public AdminMenu(ConsoleIO io, ConsolePrinter printer, UserSession session, ProductService productService,
                     InventoryService inventoryService, OrderService orderService) {
        this.io = io;
        this.printer = printer;
        this.session = session;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    private User admin() {
        return session.getCurrentUser();
    }

    public void addCategory() {
        String name = io.readLine("Category name: ");
        String description = io.readLine("Category description: ");
        Category category = productService.createCategory(admin(), name, description);
        io.println("Category created with ID: " + category.getId());
    }

    public void toggleCategory() {
        List<Category> categories = productService.getAllCategories(admin());
        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            io.println((i + 1) + ". " + c.getName() + (c.isActive() ? " [active]" : " [inactive]"));
        }
        int index = io.chooseIndex("Select a category to enable/disable", categories.size());
        if (index >= 0) {
            Category c = categories.get(index);
            productService.setCategoryActive(admin(), c.getId(), !c.isActive());
            io.println("Category '" + c.getName() + "' is now " + (c.isActive() ? "ACTIVE" : "INACTIVE") + ".");
        }
    }

    public void addProduct() {
        List<Category> categories = productService.getAllCategories(admin());
        if (categories.isEmpty()) {
            io.println("No categories found. Create a category first!");
            return;
        }
        for (int i = 0; i < categories.size(); i++) {
            io.println((i + 1) + ". " + categories.get(i).getName());
        }
        int index = io.chooseIndex("Select category", categories.size());
        if (index < 0) {
            return;
        }
        String title = io.readLine("Product title: ");
        String description = io.readLine("Description: ");
        String brand = io.readLine("Brand: ");
        BigDecimal price = io.readMoney("Price: ", "The price");
        int stock = io.readInt("Stock: ");
        Product product = productService.createProduct(admin(), title, description, price, stock, brand, categories.get(index));
        io.println("Product added successfully with ID: " + product.getId());
    }

    public void editProduct() {
        String productId = io.readLine("Enter Product ID to edit: ");
        Product product = productService.getProductById(productId);
        io.println("Editing: " + product.getTitle() + " (leave empty to keep the current value)");
        String title = io.readLine("New title [" + product.getTitle() + "]: ");
        String description = io.readLine("New description [" + product.getDescription() + "]: ");
        BigDecimal price = io.readOptionalMoney("New price [" + product.getPrice() + "]: ", "The price");
        Integer stock = io.readOptionalInt("New stock [" + product.getStock() + "]: ");
        String brand = io.readLine("New brand [" + product.getBrand() + "]: ");
        productService.updateProduct(admin(), productId,
                title.isEmpty() ? product.getTitle() : title,
                description.isEmpty() ? product.getDescription() : description,
                price == null ? product.getPrice() : price,
                stock == null ? product.getStock() : stock,
                brand.isEmpty() ? product.getBrand() : brand,
                null);
        io.println("Product updated successfully!");
    }

    public void toggleProduct() {
        String productId = io.readLine("Enter Product ID to enable/disable: ");
        Product product = productService.getProductById(productId);
        productService.setProductActive(admin(), productId, !product.isActive());
        io.println("Product '" + product.getTitle() + "' is now " + (product.isActive() ? "ACTIVE" : "INACTIVE") + ".");
    }

    public void viewAllProducts() {
        printer.printProducts(productService.getAllProductsForAdmin(admin()));
    }

    public void viewInventory() {
        printer.printInventory(inventoryService.viewInventory(admin()));
    }

    public void changeStock() {
        String productId = io.readLine("Enter Product ID: ");
        io.println("1. Increase stock");
        io.println("2. Decrease stock");
        io.println("3. Set exact stock");
        io.println("4. Show stock history");
        String choice = io.readLine("Select an option: ");
        if (choice.equals("4")) {
            for (StockMovement m : inventoryService.getStockHistory(admin(), productId)) {
                io.printf("%s | %+d -> %d | %s%n", m.getTimestamp(), m.getDelta(), m.getResultingStock(), m.getReason());
            }
            return;
        }
        int amount = io.readInt(choice.equals("3") ? "New stock: " : "Amount: ");
        String reason = io.readLine("Reason: ");
        Product product;
        switch (choice) {
            case "1": product = inventoryService.increaseStock(admin(), productId, amount, reason); break;
            case "2": product = inventoryService.decreaseStock(admin(), productId, amount, reason); break;
            case "3": product = inventoryService.setStock(admin(), productId, amount, reason); break;
            default:
                io.println("Invalid option.");
                return;
        }
        io.println("Stock updated. Current stock of '" + product.getTitle() + "': " + product.getStock());
    }

    public void viewOrders() {
        OrderStatus[] statuses = OrderStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            io.println((i + 1) + ". " + statuses[i]);
        }
        Integer choice = io.readOptionalInt("Filter by status number (Enter for all orders): ");
        List<Order> orders;
        if (choice == null) {
            orders = orderService.getAllOrders(admin());
        } else if (choice >= 1 && choice <= statuses.length) {
            orders = orderService.getOrdersByStatus(admin(), statuses[choice - 1]);
        } else {
            throw new IllegalArgumentException("Invalid status selection.");
        }
        if (orders.isEmpty()) {
            io.println("No orders found.");
            return;
        }
        for (Order o : orders) {
            io.printf("Order %s | User %s | %s | $%.2f%n", o.getId(), o.getUserId(), o.getStatus(), o.getTotalAmount());
        }
    }

    public void viewOrderDetails() {
        String orderId = io.readLine("Enter Order ID: ");
        printer.printOrderDetails(orderService.getOrderDetails(admin(), orderId), true);
    }

    public void updateOrderStatus() {
        String orderId = io.readLine("Order ID: ");
        Order order = orderService.getOrderDetails(admin(), orderId);
        List<OrderStatus> options = new ArrayList<>(order.getStatus().getAllowedNextStatuses());
        options.remove(OrderStatus.PAID);
        if (options.isEmpty()) {
            io.println("Order status is " + order.getStatus() + "; no further changes are possible.");
            return;
        }
        io.println("Current status: " + order.getStatus() + ". Allowed next statuses:");
        for (int i = 0; i < options.size(); i++) {
            io.println((i + 1) + ". " + options.get(i));
        }
        int index = io.chooseIndex("Select the new status", options.size());
        if (index >= 0) {
            orderService.updateOrderStatus(admin(), orderId, options.get(index));
            io.println("Order status updated to " + order.getStatus() + ".");
        }
    }
}
