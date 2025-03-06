package com.example.service;

import com.example.model.Order;
import com.example.model.Product;
import com.example.repository.OrderRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Service
public class OrderService extends MainService<Order>{

    //The Dependency Injection Variables
//The Constructor with the requried variables mapping the Dependency Injection.

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService( OrderRepository orderRepository) {
        super(orderRepository, "Order");
        this.orderRepository = orderRepository;
    }

   public void addOrder(Order order) {
        orderRepository.addOrder(order);
    }
    public ArrayList<Order> getOrders(){
        return orderRepository.getOrders();
    }
    public Order getOrderById(UUID orderId){
        return orderRepository.getOrderById(orderId);
    }
    public void deleteOrderById(UUID orderId) throws IllegalArgumentException{
//        validateExistence(orderId);
        orderRepository.deleteOrderById(orderId);
    }
}
