package com.example.repository;

import com.example.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.example.model.Order;

@Repository
@SuppressWarnings("rawtypes")
public class UserRepository extends MainRepository<User> {



    public ArrayList<User> getUsers() {
        return findAll();
    }

    public User getUserById(UUID userId) {
       return findById(userId);
    }
    public User addUser(User user) {
        return create(user);
    }
    public List<Order> getOrdersByUserId(UUID userId) {
        if (findById(userId)==null){
            throw new NoSuchElementException("User with ID " + userId + " not found");
        }
        return findById(userId).getOrders();
    }

    public void addOrderToUser(UUID userId, Order order) {
        if (findById(userId)==null) {
            throw new NoSuchElementException("User with ID " + userId + " not found");
        }
        updateById(userId, user -> user.getOrders().add(order));
    }
    public void removeOrderFromUser(UUID userId, UUID orderId) {
        if (findById(userId)==null) {
            throw new NoSuchElementException("User with ID " + userId + " not found");
        }
        updateById(userId, user -> user.getOrders().removeIf(order -> order.getId().equals(orderId)));
    }
    public void deleteUserById(UUID userId) {
        deleteById(userId);
    }


    @Override
    protected String getDataPath() {
        return "data/users.json";
    }

    @Override
    protected Class<User[]> getArrayType() {
        return User[].class;
    }

    @Override
    public UUID getIdFromModel(User model) {
        return model.getId();
    }

    @Override
    public void setIdForModel(User model, UUID id) {
        model.setId(id);
    }
}
