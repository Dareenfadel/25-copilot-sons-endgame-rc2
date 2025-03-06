package com.example.repository;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.model.Product;

@Repository
public class ProductRepository extends MainRepository<Product> {

    // ----------------------
    // CRUD Operations
    // ----------------------

    public Product addProduct(Product product) {
        return create(product);
    }

    public ArrayList<Product> getProducts() {
        return findAll();
    }

    public Product getProductById(UUID productId) {
        return findById(productId);
    }

    public Product updateProduct(UUID productId, String newName, double newPrice) {
        return updateById(productId, (product) -> {
            product.setName(newName);
            product.setPrice(newPrice);
        });
    }

    public void deleteProductById(UUID productId) {
        deleteById(productId);
    }

    // ----------------------
    // Business Operations
    // ----------------------

    public void applyDiscount(double discount, ArrayList<UUID> productIds) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }

        updateWhere(
                (product) -> productIds.contains(product.getId()),
                (product) -> product.applyDiscount(discount));
    }

    // ----------------------
    // Repository Configuration
    // ----------------------

    @Override
    public UUID getIdFromModel(Product model) {
        return model.getId();
    }

    @Override
    public void setIdForModel(Product model, UUID id) {
        model.setId(id);
    }

    @Override
    protected String getDataPath() {
        return "src/main/java/com/example/data/products.json";
    }

    @Override
    protected Class<Product[]> getArrayType() {
        return Product[].class;
    }
}
