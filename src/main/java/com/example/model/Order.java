package com.example.model;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Order {
    private UUID id;
    private UUID userId;
    private double totalPrice;
    private List<Product> products=new ArrayList<>();
    public Order(UUID id, UUID userId, double totalPrice, List<Product> products) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.products = products;
    }

    public UUID getId() {
        return id;
    }
}
