package com.example.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.Product;
import com.example.repository.ProductRepository;

@Service
public class ProductService extends MainService<Product> {

    // ----------------------
    // Dependency Injection & Constructor
    // ----------------------

    private ProductRepository productRepository;
    private CartService cartService;
    
    public ProductService(ProductRepository productRepository, CartService cartService) {
        super(productRepository, "Product");
        this.productRepository = productRepository;
        this.cartService = cartService;
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
        cartService.updateEach((cart) -> replaceModelsInList(cart.getProducts(), newProduct));

        return newProduct;
    }

    public void deleteProductById(UUID productId) {
        // Delete the product from the product repository
        productRepository.deleteProductById(productId);

        // Cascade the deletion to all carts that contain the product
        cartService.updateEach((cart) -> deleteModelFromListById(cart.getProducts(), productId));
    }

    // ----------------------
    // Business Operations
    // ----------------------

    public void applyDiscount(double discount, ArrayList<UUID> productIds) {
        productRepository.applyDiscount(discount, productIds);

        // Cascade the discount to all carts that contain the products
        cartService.updateEach((cart) -> {
            for (var product : cart.getProducts()) {
                if (productIds.contains(product.getId())) {
                    product.applyDiscount(discount);
                }
            }
        });
    }

    // ----------------------
    // Getters & Setters
    // ----------------------
    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }
}
