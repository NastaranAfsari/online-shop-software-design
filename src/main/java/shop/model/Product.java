package shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Entity: a product of the shop. It protects all of its own invariants. */
public class Product {
    private static final String DEFAULT_BRAND = "Generic";

    private final String id;
    private String title;
    private String description;
    private BigDecimal price;
    private int stock;
    private String brand;
    private Category category;
    private final LocalDateTime createdAt;
    private final List<Review> reviews = new ArrayList<>();
    private final List<String> imageUrls = new ArrayList<>();
    private final List<StockMovement> stockHistory = new ArrayList<>();
    private boolean active = true;
    private int soldCount;

    public Product(String title, String description, BigDecimal price, int stock, String brand, Category category) {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        applyDetails(title, description, price, brand, category);
        if (stock < 0) {
            throw new IllegalArgumentException("The stock cannot be negative.");
        }
        this.stock = stock;
        if (stock > 0) {
            stockHistory.add(new StockMovement(id, stock, stock, "Initial stock", "system"));
        }
    }

    // ---------- details ----------

    /** Replaces all editable details at once; nothing changes if validation fails. */
    public void updateDetails(String title, String description, BigDecimal price, String brand, Category category) {
        applyDetails(title, description, price, brand, category);
    }

    private void applyDetails(String newTitle, String newDescription, BigDecimal newPrice,
                              String newBrand, Category newCategory) {
        String validTitle = Validators.requireNonBlank(newTitle, "Product title");
        BigDecimal validPrice = Money.normalize(newPrice, "The price");
        if (newCategory == null) {
            throw new IllegalArgumentException("A product must have a category.");
        }
        this.title = validTitle;
        this.description = newDescription == null ? "" : newDescription.trim();
        this.price = validPrice;
        this.brand = Validators.isBlank(newBrand) ? DEFAULT_BRAND : newBrand.trim();
        this.category = newCategory;
    }

    public void addImage(String url) {
        imageUrls.add(Validators.requireNonBlank(url, "Image URL"));
    }

    // ---------- stock ----------

    public void increaseStock(int count, String reason, String performedBy) {
        Validators.requirePositive(count, "Count");
        stock += count;
        stockHistory.add(new StockMovement(id, count, stock, reason, performedBy));
    }

    public void decreaseStock(int count, String reason, String performedBy) {
        Validators.requirePositive(count, "Count");
        if (count > stock) {
            throw new IllegalArgumentException("Insufficient stock.");
        }
        stock -= count;
        stockHistory.add(new StockMovement(id, -count, stock, reason, performedBy));
    }

    /** Sets the stock to an exact value (admin correction); the difference is logged. */
    public void adjustStockTo(int newStock, String reason, String performedBy) {
        if (newStock < 0) {
            throw new IllegalArgumentException("The stock cannot be negative.");
        }
        int delta = newStock - stock;
        if (delta == 0) {
            return;
        }
        stock = newStock;
        stockHistory.add(new StockMovement(id, delta, stock, reason, performedBy));
    }

    public void recordSale(int count) {
        soldCount += Validators.requirePositive(count, "Count");
    }

    public void revertSale(int count) {
        soldCount = Math.max(0, soldCount - Validators.requirePositive(count, "Count"));
    }

    // ---------- reviews ----------

    /** Adds the review or replaces the previous review of the same user. */
    public void putReview(Review review) {
        reviews.removeIf(r -> r.getUserId().equals(review.getUserId()));
        reviews.add(review);
    }

    public Optional<Review> findReviewByUser(String userId) {
        return reviews.stream().filter(r -> r.getUserId().equals(userId)).findFirst();
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    /** Only the reviews that contain a comment text. */
    public List<Review> getComments() {
        List<Review> comments = new ArrayList<>();
        for (Review review : reviews) {
            if (review.hasComment()) {
                comments.add(review);
            }
        }
        return Collections.unmodifiableList(comments);
    }

    public int getRatingCount() {
        int count = 0;
        for (Review review : reviews) {
            if (review.hasScore()) {
                count++;
            }
        }
        return count;
    }

    public double getAverageRating() {
        int count = 0;
        double sum = 0;
        for (Review review : reviews) {
            if (review.hasScore()) {
                sum += review.getScore();
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    // ---------- state ----------

    /** A product can be bought only if it is active and in stock. */
    public boolean isAvailable() {
        return active && stock > 0;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // ---------- getters ----------

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getBrand() {
        return brand;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public List<String> getImageUrls() {
        return Collections.unmodifiableList(imageUrls);
    }

    public List<StockMovement> getStockHistory() {
        return Collections.unmodifiableList(stockHistory);
    }
}
