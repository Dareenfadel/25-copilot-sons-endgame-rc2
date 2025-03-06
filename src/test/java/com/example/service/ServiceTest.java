package com.example.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.io.TempDir;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.Product;
import com.example.model.User;
import com.example.repository.CartRepository;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ServiceTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    private Path userTempDir;

    @TempDir
    private Path cartTempDir;

    @TempDir
    private Path productTempDir;
    
    @TempDir
    private Path orderTempDir;
    
    protected UserRepository userRepository;
    protected ProductRepository productRepository;
    protected OrderRepository orderRepository;
    protected CartRepository cartRepository;
    
    protected UserService userService;
    protected ProductService productService;
    protected OrderService orderService;
    protected CartService cartService;
    
    public ServiceTest() {
        productRepository = new ProductRepository() {
            @Override
            protected String getDataPath() {
                return getTestProductDataFilePath().toString();
            }
        };
        cartRepository = new CartRepository() {
            @Override
            protected String getDataPath() {
                return getTestCartDataFilePath().toString();
            }
        };
        orderRepository = new OrderRepository() {
            @Override
            protected String getDataPath() {
                return getTestOrderDataFilePath().toString();
            }
        };
        userRepository = new UserRepository() {
            @Override
            protected String getDataPath() {
                return getTestUserDataFilePath().toString();
            }
        };
        
        orderService = new OrderService(orderRepository);
        productService = new ProductService(productRepository, null);
        cartService = new CartService(cartRepository, productService, null);
        userService = new UserService(userRepository, cartService, orderService, productService);
        
        productService.setCartService(cartService);
        cartService.setUserService(userService);
    }
    
    protected List<User> readTestUserData() throws IOException {
        return objectMapper.readValue(getTestUserDataFilePath().toFile(), new TypeReference<List<User>>() {});
    }
    protected List<Cart> readTestCartData() throws IOException {
        return objectMapper.readValue(getTestCartDataFilePath().toFile(), new TypeReference<List<Cart>>() {});
    }
    protected List<Product> readTestProductData() throws IOException {
        return objectMapper.readValue(getTestProductDataFilePath().toFile(), new TypeReference<List<Product>>() {});
    }
    protected List<Order> readTestOrderData() throws IOException {
        return objectMapper.readValue(getTestOrderDataFilePath().toFile(), new TypeReference<List<Order>>() {});
    }
    protected void writeTestUserData(List<User> users) throws IOException {
        objectMapper.writeValue(getTestUserDataFilePath().toFile(), users);
    }
    protected void writeTestOrderData(List<Order> orders) throws IOException {
        objectMapper.writeValue(getTestOrderDataFilePath().toFile(), orders);
    }
    protected void writeTestCartData(List<Cart> carts) throws IOException {
        objectMapper.writeValue(getTestCartDataFilePath().toFile(), carts);
    }
    protected void writeTestProductData(List<Product> products) throws IOException {
        objectMapper.writeValue(getTestProductDataFilePath().toFile(), products);
    }

    protected Path getTestUserDataFilePath() {
        return userTempDir.resolve("users.json");
    }
    protected Path getTestCartDataFilePath() {
        return cartTempDir.resolve("carts.json");
    }
    protected Path getTestProductDataFilePath() {
        return productTempDir.resolve("products.json");
    }
    protected Path getTestOrderDataFilePath() {
        return orderTempDir.resolve("orders.json");
    }
}
