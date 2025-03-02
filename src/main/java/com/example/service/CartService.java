package com.example.service;

import com.example.model.Cart;
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        return cartRepository.addCart(cart);
    }
    public ArrayList<Cart> getCarts(){
        return cartRepository.getCarts();

    }
    public Cart getCartById(UUID cartId){
        Cart cart= cartRepository.getCartById(cartId);
        if(cart==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found!");
        }
        return cart;
    }
    public Cart getCartByUserId(UUID userId){
        User user = userRepository.getUserById(userId);
        if(user==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        return cartRepository.getCartByUserId(userId);
    }
    public void addProductToCart(UUID cartId, Product product){
        Product existingProduct = productRepository.getProductById(product.getId());
        if (existingProduct == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found!");
        }

        cartRepository.addProductToCart(cartId, product);

    }
    public void deleteProductFromCart(UUID cartId, Product product){
        Product existingProduct = productRepository.getProductById(product.getId());
        if (existingProduct == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found!");
        }
        cartRepository.deleteProductFromCart(cartId, product);


    }
    public void deleteCartById(UUID cartId){
        Cart cart = cartRepository.getCartById(cartId);
        if(cart==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found!");
        }
        cartRepository.deleteCartById(cartId);
    }



}