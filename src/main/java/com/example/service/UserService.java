package com.example.service;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@SuppressWarnings("rawtypes")
public class UserService extends MainService<User>{
    private final UserRepository userRepository;
    private final CartService cartService;
    private final OrderService orderService;
    @Autowired
    public UserService(UserRepository userRepository, CartService cartService, OrderService orderService) {
        this.userRepository = userRepository;
        this.cartService =  cartService;
        this.orderService = orderService;
    }

        public User addUser(User user) {
            return userRepository.addUser(user);
        }


        public ArrayList<User> getUsers() {
            return userRepository.getUsers();
        }


        public User getUserById(UUID userId) {
            return userRepository.getUserById(userId);
        }


        public List<Order> getOrdersByUserId(UUID userId) {
            return userRepository.getOrdersByUserId(userId);
        }

   //checkout  logic add order to user,empty cart, add order to orders
        public void  addOrderToUser(UUID userId) {
         Order order= cartService.checkoutCart(userId);
          userRepository.addOrderToUser(userId, order);
          orderService.addOrder(order);

        }


        public void emptyCart(UUID userId) {
            cartService.emptyCart(userId);

        }


        public void removeOrderFromUser(UUID userId, UUID orderId) {
          userRepository.removeOrderFromUser(userId, orderId);


        }


        public void deleteUserById(UUID userId) {
            userRepository.deleteUserById(userId);
        }

    public void deleteProductFromCart(UUID userId, UUID productId) {
        Cart cart= cartService.getCartByUserId(userId);
        cartService.deleteProductFromCart(cart.getId(), productId);

    }
    public void addProductToCart(UUID userId, UUID productId) {
        Cart cart= cartService.getCartByUserId(userId);
        cartService.addProductToCart(cart.getId(), productId);
    }

}


