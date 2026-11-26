package shop.model;

import java.math.BigDecimal;

/** Immutable value object describing the filters of story 11. Null means "no restriction". */
public final class ProductFilter {
    private final String categoryId;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final String brand;
    private final Double minRating;
    private final boolean onlyAvailable;

    public ProductFilter(String categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                         String brand, Double minRating, boolean onlyAvailable) {
        this.categoryId = Validators.isBlank(categoryId) ? null : categoryId.trim();
        this.minPrice = minPrice == null ? null : Money.normalize(minPrice, "Minimum price");
        this.maxPrice = maxPrice == null ? null : Money.normalize(maxPrice, "Maximum price");
        this.brand = Validators.isBlank(brand) ? null : brand.trim();
        if (minRating != null && (minRating < 1.0 || minRating > 5.0)) {
            throw new IllegalArgumentException("The rating filter must be between 1 and 5.");
        }
        this.minRating = minRating;
        this.onlyAvailable = onlyAvailable;
        if (this.minPrice != null && this.maxPrice != null && this.minPrice.compareTo(this.maxPrice) > 0) {
            throw new IllegalArgumentException("The minimum price must not be higher than the maximum price.");
        }
    }

    public static ProductFilter none() {
        return new ProductFilter(null, null, null, null, null, false);
    }

    public String getCategoryId() {
        return categoryId;
    }

    public boolean matches(Product product) {
        return (categoryId == null || product.getCategory().getId().equals(categoryId))
                && (minPrice == null || product.getPrice().compareTo(minPrice) >= 0)
                && (maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0)
                && (brand == null || product.getBrand().equalsIgnoreCase(brand))
                && (minRating == null || product.getAverageRating() >= minRating)
                && (!onlyAvailable || product.isAvailable());
    }
}
