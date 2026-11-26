package shop.service;

import shop.model.Product;

/**
 * Narrow read-only view of the catalog for services that only need to find a product (ISP).
 * Implemented by ProductService.
 */
public interface ProductLookup {
    /** Returns the product (active or inactive) or throws if no product has this id. */
    Product getProductById(String id);
}
