package com.example.service;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.Product;
import com.example.model.User;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@SuppressWarnings("rawtypes")
public class CartService extends MainService<Cart>{
//The Dependency Injection Variables
//The Constructor with the requried variables mapping the Dependency Injection.
    CartRepository cartRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    @Autowired
    public CartService(CartRepository cartRepository, ProductRepository productRepository, UserRepository userRepository){

        this.cartRepository = cartRepository;
        this.productRepository =  productRepository;
        this.userRepository = userRepository;


    }
    public Cart addCart(Cart cart){

        User user = userRepository.getUserById(cart.getUserId());
        if(user==null){
            throw new NoSuchElementException("User not found!");
        }
        Cart existingCart = cartRepository.getCartByUserId(cart.getUserId());
        if(existingCart!=null){
            throw new IllegalArgumentException("Cart already exists!");
        }
        return cartRepository.addCart(cart);
    }
    public ArrayList<Cart> getCarts(){
        return cartRepository.getCarts();

    }
    public Cart getCartById(UUID cartId){
        return cartRepository.getCartById(cartId);
    }
    public Cart getCartByUserId(UUID userId){
        User user = userRepository.getUserById(userId);
        if(user==null){
            throw new NoSuchElementException( "User not found!");
        }
        Cart cart= cartRepository.getCartByUserId(userId);
        if(cart==null)
            return addCart(new Cart(userId));
        else return cart;
    }
    public void addProductToCart(UUID cartId, Product product){
        //if product not exist add to products first instead of exception
        Product existingProduct = productRepository.getProductById(product.getId());
        if (existingProduct == null) {
            throw new NoSuchElementException("Product not found!");
        }
        cartRepository.addProductToCart(cartId, product);
    }
    public void deleteProductFromCart(UUID cartId, Product product){
//        Product existingProduct = productRepository.getProductById(product.getId());
//        if (existingProduct == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found!");
//        }
        cartRepository.deleteProductFromCart(cartId, product);
    }
    public void deleteCartById(UUID cartId){
//        Cart cart = cartRepository.getCartById(cartId);
//        if(cart==null){
//            throw new NoSuchElementException( "Cart not found!");
//        }
        cartRepository.deleteCartById(cartId);
    }
        public Order checkoutCart(UUID userId) {
                List<Product> cartItems = cartRepository.getCartByUserId(userId).getProducts();
                if (cartItems.isEmpty()) {
                        throw new IllegalArgumentException("Cart is empty");
                }
                double totalAmount = cartItems.stream()
                        .mapToDouble(Product::getPrice)
                        .sum();
                Order newOrder = new Order(userId, totalAmount, cartItems);
                cartRepository.emptyCart(userId);
                return newOrder;

        }
        public void emptyCart(UUID userId) {
                cartRepository.emptyCart(userId);
        }



}