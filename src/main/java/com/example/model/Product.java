package com.example.model;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class Product {
    // ----------------------
    // Instance variables
    // ----------------------
    private UUID id;
    private String name;
    private double price;

    // ----------------------
    // Constructors
    // ----------------------
    public Product() {
    }

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public Product(UUID id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // ----------------------
    // Business Logic
    // ----------------------
    public void applyDiscount(double discount) {
        this.price= this.price - (this.price * discount / 100);
    }

    // ----------------------
    // Getters and Setters
    // ----------------------
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // ----------------------
    // Overridden Methods
    // ----------------------

    @Override
    public boolean equals(Object otherProduct) {
        if (this == otherProduct) {
            return true;
        }

        if (otherProduct == null || !(otherProduct instanceof Product)) {
            return false;
        }

        Product product = (Product) otherProduct;
        return id.equals(product.id) &&
                name.equals(product.name) &&
                price == product.price;
    }


}
