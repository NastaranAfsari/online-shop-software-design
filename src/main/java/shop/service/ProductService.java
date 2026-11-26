package shop.service;

import shop.model.Category;
import shop.model.Product;
import shop.model.ProductFilter;
import shop.model.ProductSortOption;
import shop.model.User;
import shop.model.Validators;
import shop.repository.CategoryRepository;
import shop.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/** Catalog use cases: categories, browsing, search, filter/sort (customers) and management (admin). */
public class ProductService implements ProductLookup {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ---------- categories ----------

    public Category createCategory(User actor, String name, String description) {
        Authorization.requireAdmin(actor);
        Category category = new Category(name, description);
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new IllegalArgumentException("A category with this name already exists.");
        }
        return categoryRepository.save(category);
    }

    public void setCategoryActive(User actor, String categoryId, boolean active) {
        Authorization.requireAdmin(actor);
        Category category = getCategoryById(categoryId);
        category.setActive(active);
        categoryRepository.save(category);
    }

    /** Only active categories are shown to customers (story 13). */
    public List<Category> getActiveCategories() {
        return categoryRepository.findAll().stream().filter(Category::isActive).collect(Collectors.toList());
    }

    public List<Category> getAllCategories(User actor) {
        Authorization.requireAdmin(actor);
        return categoryRepository.findAll();
    }

    public Category getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("The selected category does not exist."));
    }

    // ---------- customer queries ----------

    public List<Product> getActiveProducts() {
        return productRepository.findAll().stream().filter(Product::isActive).collect(Collectors.toList());
    }

    /** Internal lookup used by other services (returns active and inactive products). */
    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No product with the specified ID was found."));
    }

    public List<Product> searchProducts(String keyword) {
        if (Validators.isBlank(keyword)) {
            return getActiveProducts();
        }
        if (keyword.trim().length() > Validators.MAX_SEARCH_LENGTH) {
            throw new IllegalArgumentException(
                    "The search phrase cannot be longer than " + Validators.MAX_SEARCH_LENGTH + " characters.");
        }
        String lower = keyword.trim().toLowerCase();
        return getActiveProducts().stream()
                .filter(p -> p.getTitle().toLowerCase().contains(lower)
                        || p.getBrand().toLowerCase().contains(lower)
                        || p.getDescription().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Product> getProductsByCategory(String categoryId) {
        Category category = getCategoryById(categoryId);
        if (!category.isActive()) {
            throw new IllegalArgumentException("The selected category is not active.");
        }
        return productRepository.findByCategoryId(categoryId).stream()
                .filter(Product::isActive).collect(Collectors.toList());
    }

    /** Filter + sort among active products. A null sort option keeps the default order. */
    public List<Product> filterAndSortProducts(ProductFilter filter, ProductSortOption sortOption) {
        ProductFilter effective = filter == null ? ProductFilter.none() : filter;
        if (effective.getCategoryId() != null) {
            getCategoryById(effective.getCategoryId());
        }
        List<Product> result = getActiveProducts().stream().filter(effective::matches).collect(Collectors.toList());
        if (sortOption != null) {
            result.sort(sortOption.getComparator());
        }
        return result;
    }

    // ---------- admin ----------

    public Product createProduct(User actor, String title, String description, BigDecimal price,
                                 int stock, String brand, Category category) {
        Authorization.requireAdmin(actor);
        return productRepository.save(new Product(title, description, price, stock, brand, category));
    }

    /** A null category keeps the current one. Validation happens before anything is changed. */
    public Product updateProduct(User actor, String productId, String title, String description,
                                 BigDecimal price, int stock, String brand, Category category) {
        Authorization.requireAdmin(actor);
        Product product = getProductById(productId);
        if (stock < 0) {
            throw new IllegalArgumentException("The stock cannot be negative.");
        }
        product.updateDetails(title, description, price, brand,
                category != null ? category : product.getCategory());
        product.adjustStockTo(stock, "Edited by admin", actor.getId());
        return productRepository.save(product);
    }

    /** Deactivated products stay in the system (old orders keep their data) but cannot be bought. */
    public void setProductActive(User actor, String productId, boolean active) {
        Authorization.requireAdmin(actor);
        Product product = getProductById(productId);
        product.setActive(active);
        productRepository.save(product);
    }

    public List<Product> getAllProductsForAdmin(User actor) {
        Authorization.requireAdmin(actor);
        return productRepository.findAll();
    }
}
