package com.example.model;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class Product {
    private UUID id;
    private String name;
    private double price;

    public Product(UUID id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    public Product( String name, double price) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.price = price;
    }
    public Product() {
        this.id = UUID.randomUUID(); // Optional, ensures an ID is generated
    }
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
