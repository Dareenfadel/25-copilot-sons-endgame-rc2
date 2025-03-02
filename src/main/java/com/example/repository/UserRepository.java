package com.example.repository;

import com.example.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.model.Order;

@Repository
@SuppressWarnings("rawtypes")
public class UserRepository extends MainRepository<User> {

    public UserRepository() {

        }

    public ArrayList<User> getUsers() {
        return findAll();
    }

    public User getUserById(UUID userId) {
         return findAll().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }
    public User addUser(User user) {
        if (findAll().stream().noneMatch(u -> u.getId().equals(user.getId()))) {
            save(user);
        }
        return user;
    }
    public List<Order> getOrdersByUserId(UUID userId) {
        User user = getUserById(userId);
        return user != null ? user.getOrders() : new ArrayList<>();
    }

    public void addOrderToUser(UUID userId, Order order) {
        ArrayList<User> users = findAll();
        for (User user : users) {
            if (user.getId().equals(userId)) {
                user.getOrders().add(order);
                saveAll(users);
                return;
            }
        }
    }
    public void removeOrderFromUser(UUID userId, UUID orderId) {
        ArrayList<User> users = findAll();
        for (User user : users) {
            if (user.getId().equals(userId)) {
                user.getOrders().removeIf(order -> order.getId().equals(orderId));
                saveAll(users);
                return;
            }
        }
    }
    public void deleteUserById(UUID userId) {
        ArrayList<User> users = findAll();
        users.removeIf(user -> user.getId().equals(userId));
        saveAll(users);
    }


    @Override
    protected String getDataPath() {
        return "data/users.json";
    }

    @Override
    protected Class<User[]> getArrayType() {
        return User[].class;
    }

}
