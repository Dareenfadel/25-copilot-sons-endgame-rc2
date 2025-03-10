package com.example.service;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.Product;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@SuppressWarnings("rawtypes")
public class UserService extends MainService<User>{
    private final UserRepository userRepository;
    private final CartService cartService;
    private final OrderService orderService;
    private final ProductService productService;
    @Autowired
    public UserService(UserRepository userRepository, CartService cartService, OrderService orderService,ProductService productService){
        super(userRepository, "User");
        this.userRepository = userRepository;
        this.cartService =  cartService;
        this.orderService = orderService;
        this.productService = productService;
    }

        public User addUser(User user) {
        List<Order> orders=user.getOrders();
        if(orders!=null){
            for(Order order: orders){
                  if(orderService.getOrderById(order.getId())==null)
                      orderService.addOrder(order);
            }    }
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
           User user=  getUserById(userId);
             if(user==null)
                    throw new NoSuchElementException("User not found");
            cartService.emptyCart(userId);

        }


        public void removeOrderFromUser(UUID userId, UUID orderId) {

          userRepository.removeOrderFromUser(userId, orderId);
          if(orderService.getOrderById(orderId)!=null){
              orderService.deleteOrderById(orderId);
          }
    }


        public void deleteUserById(UUID userId) {
        Cart cart= cartService.getCartByUserId(userId);
        if(cart!=null){
            cartService.deleteCartById(cart.getId());
        }
        List<Order> orders= getOrdersByUserId(userId);
        if(orders!=null){
            for(Order order: orders){
                orderService.deleteOrderById(order.getId());
            }
        }
            userRepository.deleteUserById(userId);
        }

        //needed by the controller :)
    public String deleteProductFromCart(UUID userId, UUID productId) {
        Cart cart= cartService.getCartByUserId(userId);

            if (cart == null) {
                throw new NoSuchElementException("cart not found");
            }
         int size= cart.getProducts().size();
        if(cart.getProducts().size()==0){
            return "Cart is empty";
        }
        Product product= productService.getProductById(productId);
        if (product == null) {
            throw new NoSuchElementException("product not found");
        }
        cartService.deleteProductFromCart(cart.getId(), product);

        return "Product deleted from cart";
    }
    public void addProductToCart(UUID userId, UUID productId) {
        Cart cart= cartService.getCartByUserId(userId);
        //create new cart if not exists
        if (cart == null) {
            cart =new Cart();
            cart.setUserId(userId);
            cartService.addCart(cart);
            cart=cartService.getCartByUserId(userId);
        }

        Product product= productService.getProductById(productId);
        if (product == null) {
            throw new NoSuchElementException("product not found");
        }
        cartService.addProductToCart(cart.getId(), product);
    }

}


