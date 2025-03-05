package com.example.repository;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.UUID;

@Repository
public class OrderRepository extends MainRepository<Order>{

    public OrderRepository() {
    }

    @Override
    protected String getDataPath() {
        return DATA_DIRECTORY.resolve("orders.json").toString();
    }

    @Override
    protected Class<Order[]> getArrayType() {
        return Order[].class;
    }

    public ArrayList<Order> getOrders(){
        return findAll();
    }

    public void addOrder(Order order){
        create(order);
    }
    public Order getOrderById(UUID orderId){
       return findById(orderId);
    }
    public void deleteOrderById(UUID orderId){
        deleteById(orderId);
    }

    @Override
    public UUID getIdFromModel(Order model) {
        return model.getId();
    }
    @Override
    public void setIdForModel(Order model, UUID id) {
        model.setId(id);
    }

}
