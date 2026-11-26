package shop.service;

import shop.model.Cart;
import shop.model.Product;
import shop.model.User;

/** Shopping-cart use cases (stories 15-19). The cart used is always the one of the acting user. */
public class CartService {
    private final ProductLookup productLookup;

    public CartService(ProductLookup productLookup) {
        this.productLookup = productLookup;
    }

    public void addToCart(User user, String productId, int quantity) {
        Authorization.requireCustomer(user);
        Product product = productLookup.getProductById(productId);
        user.getCart().addItem(product, quantity);
    }

    public void updateQuantity(User user, String productId, int newQuantity) {
        Authorization.requireCustomer(user);
        user.getCart().updateQuantity(productId, newQuantity);
    }

    public void decreaseQuantity(User user, String productId, int amount) {
        Authorization.requireCustomer(user);
        user.getCart().decreaseItem(productId, amount);
    }

    public void removeFromCart(User user, String productId) {
        Authorization.requireCustomer(user);
        user.getCart().removeItem(productId);
    }

    public void clearCart(User user) {
        Authorization.requireCustomer(user);
        user.getCart().clear();
    }

    public Cart getCart(User user) {
        Authorization.requireCustomer(user);
        return user.getCart();
    }
}
