package com.example.model;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
@Component
public class User {

    private UUID id;
    private String name;
    private List<Order> orders;

    // ✅ Default constructor (Required for JSON deserialization)
    public User() {
    }

    // ✅ Constructor with all fields
    public User(UUID id, String name, List<Order> orders) {
        this.id = id;
        this.name = name;
        this.orders = orders;
    }

    // ✅ Constructor for creating a user without orders
    public User(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.orders = List.of(); // Default to an empty list
    }

    // ✅ Getters (Necessary for JSON mapping)
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Order> getOrders() {
        return orders;
    }

    // ✅ Setter for orders (Optional, if orders can be updated)
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
