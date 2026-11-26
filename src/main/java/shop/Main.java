package shop;

import shop.cli.AdminMenu;
import shop.cli.AuthMenu;
import shop.cli.CartMenu;
import shop.cli.ConsoleApp;
import shop.cli.ConsoleIO;
import shop.cli.ConsolePrinter;
import shop.cli.OrderMenu;
import shop.cli.ProductMenu;
import shop.cli.ProfileMenu;
import shop.model.Category;
import shop.model.User;
import shop.repository.InMemoryCategoryRepository;
import shop.repository.InMemoryOrderRepository;
import shop.repository.InMemoryProductRepository;
import shop.repository.InMemoryUserRepository;
import shop.service.AuthService;
import shop.service.CartService;
import shop.service.GatewayApprovalRule;
import shop.service.InventoryService;
import shop.service.OrderService;
import shop.service.PaidAmountRule;
import shop.service.PasswordHasher;
import shop.service.PasswordRecoveryService;
import shop.service.PaymentService;
import shop.service.PaymentVerificationRule;
import shop.service.Pbkdf2PasswordHasher;
import shop.service.ProductService;
import shop.service.ProfileService;
import shop.service.ReviewService;
import shop.service.SimulatedPaymentGateway;
import shop.service.StockAvailabilityRule;
import shop.service.TransactionReferenceRule;
import shop.service.UserSession;

import java.math.BigDecimal;
import java.util.List;

/** Composition root: creates every object, wires the dependencies and starts the CLI. */
public class Main {
    public static void main(String[] args) {
        // Storage (in-memory)
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryProductRepository productRepository = new InMemoryProductRepository();
        InMemoryCategoryRepository categoryRepository = new InMemoryCategoryRepository();
        InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();

        // Services
        SimulatedPaymentGateway gateway = new SimulatedPaymentGateway();
        UserSession session = new UserSession();
        PasswordHasher passwordHasher = new Pbkdf2PasswordHasher();
        AuthService authService = new AuthService(userRepository, passwordHasher, session);
        PasswordRecoveryService recoveryService = new PasswordRecoveryService(userRepository, passwordHasher);
        ProfileService profileService = new ProfileService(userRepository, session);
        ProductService productService = new ProductService(productRepository, categoryRepository);
        InventoryService inventoryService = new InventoryService(productRepository);
        CartService cartService = new CartService(productService);
        OrderService orderService = new OrderService(orderRepository, cartService, inventoryService);
        List<PaymentVerificationRule> paymentRules = List.of(new GatewayApprovalRule(), new PaidAmountRule(),
                new TransactionReferenceRule(), new StockAvailabilityRule(productService));
        PaymentService paymentService = new PaymentService(orderRepository, gateway, paymentRules, inventoryService);
        ReviewService reviewService = new ReviewService(productService, orderService);

        seedData(authService, productService);

        // Presentation
        ConsoleIO io = new ConsoleIO();
        ConsolePrinter printer = new ConsolePrinter(io);
        ConsoleApp app = new ConsoleApp(io, session,
                new AuthMenu(io, authService, recoveryService),
                new ProfileMenu(io, printer, profileService, session),
                new ProductMenu(io, printer, productService, reviewService, session),
                new CartMenu(io, printer, cartService, session),
                new OrderMenu(io, printer, session, cartService, orderService, paymentService, gateway),
                new AdminMenu(io, printer, session, productService, inventoryService, orderService));
        System.out.println("Demo accounts -> admin: admin@shop.com / admin123 | customer: ali@shop.com / 123456");
        app.start();
    }

    /** Sample data. The first admin is created here; public registration can only create customers. */
    private static void seedData(AuthService authService, ProductService productService) {
        User admin = authService.registerFirstAdmin("Admin User", "admin@shop.com", "09120000000", "admin123");
        authService.register("Ali Rezaei", "ali@shop.com", "09121111111", "123456");

        Category electronics = productService.createCategory(admin, "Electronics", "Digital products");
        Category home = productService.createCategory(admin, "Home", "Home appliances");

        productService.createProduct(admin, "Laptop Pro", "16GB RAM, 512GB SSD", price("1500"), 5, "Apple", electronics);
        productService.createProduct(admin, "Laptop Air", "8GB RAM, 256GB SSD", price("900"), 8, "Apple", electronics);
        productService.createProduct(admin, "Gaming Laptop", "RTX 3060, 16GB RAM", price("1800"), 3, "ASUS", electronics);
        productService.createProduct(admin, "Wireless Mouse", "Logitech Silent", price("25"), 50, "Logitech", electronics);
        productService.createProduct(admin, "Smart Watch", "AMOLED Screen", price("200"), 20, "Samsung", electronics);
        productService.createProduct(admin, "Coffee Maker", "Automatic 1.5L", price("150"), 10, "Philips", home);
        productService.createProduct(admin, "Air Purifier", "HEPA Filter", price("300"), 6, "Xiaomi", home);
        productService.createProduct(admin, "Desk Lamp", "LED Touch", price("45"), 30, "Xiaomi", home);
    }

    private static BigDecimal price(String value) {
        return new BigDecimal(value).setScale(2);
    }
}
