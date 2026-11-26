package shop.model;

import java.util.Comparator;

/** The only allowed sort options (story 12). Each option owns its comparator. */
public enum ProductSortOption {
    CHEAPEST("Cheapest", Comparator.comparing(Product::getPrice)),
    MOST_EXPENSIVE("Most expensive", Comparator.comparing(Product::getPrice).reversed()),
    NEWEST("Newest", Comparator.comparing(Product::getCreatedAt).reversed()),
    MOST_POPULAR("Most popular", Comparator.comparingInt(Product::getSoldCount)
            .thenComparingInt(Product::getRatingCount).reversed()),
    HIGHEST_RATED("Highest rated", Comparator.comparingDouble(Product::getAverageRating).reversed());

    private final String label;
    private final Comparator<Product> comparator;

    ProductSortOption(String label, Comparator<Product> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    public Comparator<Product> getComparator() {
        return comparator;
    }
}
