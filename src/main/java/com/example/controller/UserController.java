package com.example.controller;
import com.example.model.Order;
import com.example.model.Product;
import com.example.model.User;
import com.example.service.CartService;
import com.example.service.ProductService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    private ProductService productService;
    private CartService cartService;

    @Autowired
    public UserController(UserService userService , ProductService productService, CartService cartService) {
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
    }

    @PostMapping("/")
    public User addUser(@RequestBody User user){
        return userService.addUser(user);
    }

    @GetMapping("/")
    public ArrayList<User> getUsers(){
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable UUID userId){
        return userService.getUserById(userId);
    }

    @GetMapping("/{userId}/orders")
    public List<Order> getOrdersByUserId(@PathVariable UUID userId){
        return userService.getOrdersByUserId(userId);
    }

    @PostMapping("/{userId}/checkout")
    public String addOrderToUser(@PathVariable UUID userId){
        return userService.addOrderToUser(userId);
    }

    @PostMapping("/{userId}/removeOrder")
    public String removeOrderFromUser(@PathVariable UUID userId, @RequestParam UUID orderId){
         try {
             userService.removeOrderFromUser(userId, orderId);
             return ("Order removed");
         } catch (Exception e) {
             return(e.getMessage());
         }
    }

    @DeleteMapping("/{userId}/emptyCart")
    public String emptyCart(@PathVariable UUID userId){
        try{
            userService.emptyCart(userId);
            return("Cart emptied");
        } catch (Exception e) {
            return(e.getMessage());
        }
    }

    @PutMapping("/addProductToCart")
    public String addProductToCart(@RequestParam UUID userId, @RequestParam UUID productId){
        Product product = productService.getProductById(productId);
        try {
            cartService.addProductToCart(userId, product);
            return(product.getName()+ " added to the cart");
        } catch (Exception e) {
            return(e.getMessage());
        }
    }

    @PutMapping("/deleteProductFromCart")
    public String deleteProductFromCart(@RequestParam UUID userId, @RequestParam UUID productId){
        Product product = productService.getProductById(productId);
        try {
            cartService.deleteProductFromCart(userId, product);
            return(product.getName()+ " removed from the cart");
        } catch (Exception e) {
            return(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{userId}")
    public String deleteUserById(@PathVariable UUID userId){
        try{
            userService.deleteUserById(userId);
            return("User deleted");
        } catch (Exception e) {
            return(e.getMessage());
        }
    }

}
