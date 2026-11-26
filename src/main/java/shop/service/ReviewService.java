package shop.service;

import shop.model.Product;
import shop.model.Review;
import shop.model.User;

import java.util.List;

/** Ratings and comments (stories 30-32). Only customers who bought the product may review it. */
public class ReviewService {
    private final ProductService productService;
    private final OrderService orderService;

    public ReviewService(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    /** Creates or updates the user's rating (1-5) of the product. */
    public Review rateProduct(User user, String productId, int score) {
        Product product = requirePurchasedProduct(user, productId);
        Review review = product.findReviewByUser(user.getId())
                .map(existing -> existing.withScore(score))
                .orElseGet(() -> Review.rating(user.getId(), user.getFullName(), score));
        product.putReview(review);
        return review;
    }

    /** Creates or updates the user's comment text of the product. */
    public Review commentOnProduct(User user, String productId, String comment) {
        Product product = requirePurchasedProduct(user, productId);
        Review review = product.findReviewByUser(user.getId())
                .map(existing -> existing.withComment(comment))
                .orElseGet(() -> Review.comment(user.getId(), user.getFullName(), comment));
        product.putReview(review);
        return review;
    }

    /** Comments visible to everyone: valid, with masked reviewer names. */
    public List<Review> getComments(String productId) {
        return productService.getProductById(productId).getComments();
    }

    private Product requirePurchasedProduct(User user, String productId) {
        Authorization.requireCustomer(user);
        Product product = productService.getProductById(productId);
        if (!orderService.hasUserPurchased(user.getId(), productId)) {
            throw new IllegalStateException("Only customers who bought this product can review it.");
        }
        return product;
    }
}
