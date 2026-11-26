package shop.cli;

import shop.model.Category;
import shop.model.Product;
import shop.model.ProductFilter;
import shop.model.ProductSortOption;
import shop.service.ProductService;
import shop.service.ReviewService;
import shop.service.UserSession;

import java.math.BigDecimal;
import java.util.List;

/** Product browsing, details, search, filter/sort and reviews (stories 8-14, 30-32). */
public class ProductMenu {
    private final ConsoleIO io;
    private final ConsolePrinter printer;
    private final ProductService productService;
    private final ReviewService reviewService;
    private final UserSession session;

    public ProductMenu(ConsoleIO io, ConsolePrinter printer, ProductService productService,
                       ReviewService reviewService, UserSession session) {
        this.io = io;
        this.printer = printer;
        this.productService = productService;
        this.reviewService = reviewService;
        this.session = session;
    }

    public void browse() {
        io.println("\n=== Browse Products ===");
        io.println("1. View all products");
        io.println("2. Browse by category");
        io.println("0. Back");
        switch (io.readLine("Select an option: ")) {
            case "1":
                printer.printProducts(productService.getActiveProducts());
                break;
            case "2":
                browseByCategory();
                break;
            case "0":
                break;
            default:
                io.println("Invalid option.");
        }
    }

    private void browseByCategory() {
        List<Category> categories = productService.getActiveCategories();
        if (categories.isEmpty()) {
            io.println("No categories available.");
            return;
        }
        printCategories(categories);
        int index = io.chooseIndex("Select a category", categories.size());
        if (index < 0) {
            return;
        }
        Category chosen = categories.get(index);
        List<Product> products = productService.getProductsByCategory(chosen.getId());
        io.println("\n--- Products in category: " + chosen.getName() + " ---");
        printer.printProducts(products);
    }

    public void search() {
        String keyword = io.readLine("Enter search keyword: ");
        List<Product> results = productService.searchProducts(keyword);
        if (results.isEmpty()) {
            io.println("No products matched your search.");
            return;
        }
        printer.printProducts(results);
    }

    public void filterAndSort() {
        io.println("\n=== Filter & Sort (press Enter to skip any option) ===");
        String categoryId = null;
        List<Category> categories = productService.getActiveCategories();
        if (!categories.isEmpty()) {
            printCategories(categories);
            Integer choice = io.readOptionalInt("Category number: ");
            if (choice != null) {
                if (choice < 1 || choice > categories.size()) {
                    throw new IllegalArgumentException("Only existing categories can be selected.");
                }
                categoryId = categories.get(choice - 1).getId();
            }
        }
        BigDecimal minPrice = io.readOptionalMoney("Minimum price: ", "Minimum price");
        BigDecimal maxPrice = io.readOptionalMoney("Maximum price: ", "Maximum price");
        String brand = io.readLine("Brand: ");
        Double minRating = io.readOptionalDouble("Minimum rating (1-5): ");
        boolean onlyAvailable = io.readLine("Only available products? (y/n): ").equalsIgnoreCase("y");
        ProductFilter filter = new ProductFilter(categoryId, minPrice, maxPrice, brand, minRating, onlyAvailable);

        ProductSortOption[] options = ProductSortOption.values();
        io.println("\nSort by:");
        for (int i = 0; i < options.length; i++) {
            io.println((i + 1) + ". " + options[i].getLabel());
        }
        Integer sortChoice = io.readOptionalInt("Select sorting (Enter for default order): ");
        ProductSortOption sort = null;
        if (sortChoice != null) {
            if (sortChoice < 1 || sortChoice > options.length) {
                throw new IllegalArgumentException("Only the listed sort options can be selected.");
            }
            sort = options[sortChoice - 1];
        }
        List<Product> results = productService.filterAndSortProducts(filter, sort);
        io.println("\n=== Results (" + results.size() + " items) ===");
        printer.printProducts(results);
    }

    public void showDetails() {
        String productId = io.readLine("Enter Product ID: ");
        Product product = productService.getProductById(productId);
        printer.printProductDetails(product, reviewService.getComments(productId));
    }

    public void rate() {
        String productId = io.readLine("Enter Product ID: ");
        int score = io.readInt("Your rating (1 to 5): ");
        reviewService.rateProduct(session.getCurrentUser(), productId, score);
        io.println("Rating saved.");
    }

    public void comment() {
        String productId = io.readLine("Enter Product ID: ");
        String text = io.readLine("Your comment: ");
        reviewService.commentOnProduct(session.getCurrentUser(), productId, text);
        io.println("Comment saved.");
    }

    private void printCategories(List<Category> categories) {
        io.println("\n--- Categories ---");
        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            io.println((i + 1) + ". " + c.getName() + (c.getDescription().isEmpty() ? "" : " (" + c.getDescription() + ")"));
        }
    }
}
