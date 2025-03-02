package com.example.service;

import com.example.model.Order;
import com.example.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartService{


    //needed for checout in user service
//    public Order checkoutCart(UUID userId) {
//        List<Product> cartItems = cartRepository.getCartByUserId(userId).getProducts();
//        if (cartItems.isEmpty()) {
//            throw new IllegalStateException("Cart is empty");
//        }
//        double totalAmount = cartItems.stream()
//                .mapToDouble(Product::getPrice)
//                .sum();
//        Order newOrder = new Order(UUID.randomUUID(), userId, new ArrayList<>(cartItems),totalAmount);
//        cartRepository.emptyCart(userId);
//        return newOrder;
//
//    }
//    public void emptyCart(UUID userId) {
//        cartRepository.emptyCart(userId);
//    }
}
