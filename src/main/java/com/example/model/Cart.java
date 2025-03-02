package com.example.model;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class Cart {
    private UUID id;
    private UUID userId;
    private List<Product> products;

    public Cart() {
        this.id = UUID.randomUUID();
        this.products = new ArrayList<>();
    }

    // 2. Constructor with User ID (for creating a new cart for a user)
    public Cart(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.products = new ArrayList<>();
    }

    // 3. Constructor with All Fields (Full Constructor)
    public Cart(UUID id, UUID userId, List<Product> products) {
        this.id = id;
        this.userId = userId;
        this.products = products;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<Product> getProducts() {
        return products;
    }


}