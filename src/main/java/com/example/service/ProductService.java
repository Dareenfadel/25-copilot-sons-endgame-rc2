package com.example.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.model.Product;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;

@Service
public class ProductService extends MainService<Product> {

    // ----------------------
    // Dependency Injection & Constructor
    // ----------------------

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    // ----------------------
    // CRUD Operations
    // ----------------------

    public Product addProduct(Product product) {
        return productRepository.addProduct(product);
    }

    public ArrayList<Product> getProducts() {
        return productRepository.getProducts();
    }

    public Product getProductById(UUID productId) {
        return productRepository.getProductById(productId);
    }

    public Product updateProduct(UUID productId, String newName, double newPrice) {
        // Update the product in the product repository
        var newProduct = productRepository.updateProduct(productId, newName, newPrice);

        // Cascade the update to all carts that contain the product
        cartRepository.updateEach((cart) -> {
            var products = cart.getProducts();

            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId().equals(newProduct.getId())) {
                    products.set(i, newProduct);
                }
            }
        });

        return newProduct;
    }

    public void deleteProductById(UUID productId) {
        productRepository.deleteProductById(productId);
    }

    // ----------------------
    // Business Operations
    // ----------------------

    public void applyDiscount(double discount, ArrayList<UUID> productIds) {
        productRepository.applyDiscount(discount, productIds);
    }
}
