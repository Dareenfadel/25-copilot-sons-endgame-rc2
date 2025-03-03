package com.example.repository;

import com.example.model.Cart;
import com.example.model.Product;
import com.example.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

@Repository
@SuppressWarnings("rawtypes")
public class CartRepository extends MainRepository<Cart> {
    public CartRepository(){
    }
    @Override
    protected String getDataPath() {
        return DATA_DIRECTORY.resolve("carts.json").toString();
    }

    @Override
    protected Class<Cart[]> getArrayType() {
        return Cart[].class;
    }
    public Cart addCart(Cart cart){

        save(cart);
        return cart;
    }
    public ArrayList<Cart> getCarts(){
        return findAll();
    }
    public Cart getCartById(UUID cartId){
        return findAll().stream().filter(cart -> cart.getId().equals(cartId)).findFirst().orElse(null);
    }
    public Cart getCartByUserId(UUID userId){
        ArrayList<Cart> carts = findAll();
            if (carts == null) {
           return null;
            }

        return carts.stream().filter(cart -> cart.getUserId().equals(userId)).findFirst().orElse(null);
    }

    public void addProductToCart(UUID cartId, Product product){
        Cart cart = getCartById(cartId); //can I do this in service and pass it to repository?
        if(cart==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Cart not found!");
        }
        cart.getProducts().add(product);
        ArrayList<Cart> carts = getCarts();
        for (int i = 0; i < carts.size(); i++) {
            if (carts.get(i).getId().equals(cart.getId())) {
                carts.set(i, cart);
                overrideData(carts);
                return;
            }
        }
    }
    public void deleteProductFromCart(UUID cartId, Product product){
        Cart cart = getCartById(cartId);
        if(cart==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found!");
        }
        cart.getProducts().remove(product);
        ArrayList<Cart> carts = getCarts();
        for (int i = 0; i < carts.size(); i++) {
            if (carts.get(i).getId().equals(cart.getId())) {
                carts.set(i, cart);
                overrideData(carts);
                return;
            }
        }
    }
    public void deleteCartById(UUID cartId){
        ArrayList<Cart> carts = getCarts();
        carts.removeIf(cart -> cart.getId().equals(cartId));
        overrideData(carts);
    }

    public void emptyCart(UUID userId) {
        Cart cart = getCartByUserId(userId);

            if (cart == null) {
                throw new NoSuchElementException("Cart not found for user: " + userId);
            }

        cart.getProducts().clear();
        ArrayList<Cart> carts = getCarts();
        for (int i = 0; i < carts.size(); i++) {
            if (carts.get(i).getId().equals(cart.getId())) {
                carts.set(i, cart);
                overrideData(carts);
                return;
            }
        }
    }
    @Override
    public UUID getIdFromModel(Cart model) {
        return model.getId();
    }

    @Override
    public void setIdForModel(Cart model, UUID id) {
        model.setId(id);
    }
}