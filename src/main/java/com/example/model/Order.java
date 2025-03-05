package com.example.model;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Order {
    private UUID id;
    private UUID userId;
    private double totalPrice;


    private List<Product> products;

    public Order( UUID id,
                  UUID userId,
                  double totalPrice,
                  List<Product> products) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.products = (products != null) ? products : new ArrayList<>();
    }

     public Order(UUID userId, double totalPrice, List<Product> products) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.products = products;
    }
    public Order() {
        this.totalPrice = 0.0;
        this.products = new ArrayList<>();
    }
    public UUID getId() {
        return id;
    }
    public UUID getUserId() {
        return userId;
    }
    public double getTotalPrice() {
        return totalPrice;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (Double.compare(order.totalPrice, totalPrice) != 0) {  return false;}
        if (!id.equals(order.id)) { return false;}
        if (!userId.equals(order.userId)) {  System.out.print(order.userId+" "+this.userId);return false;}
        return products.equals(order.products);
    }



}
