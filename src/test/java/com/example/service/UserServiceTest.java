package com.example.service;


import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.Product;
import com.example.model.User;
import com.example.utils.TestUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceTest extends ServiceTest {

    //---------------------------
    //ADD NEW User TEST
    //---------------------------
    @Test
    public void addUser_WhenUserIdIsNull_ShouldCreateUserWithRandomId()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var user1 = new User("user 1", new ArrayList<Order>());
        var user2 = new User("user 2", new ArrayList<Order>());

        var uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

        var expectedUser1 = new User(uuid1, "user 1", new ArrayList<Order>());
        var expectedUser2 = new User(uuid2, "user 2", new ArrayList<Order>());

        var expectedUsers = List.of(expectedUser1, expectedUser2);

        // Act
        var returns = new ArrayList<User>();
        TestUtils.withMockedUuids(List.of(uuid1, uuid2), () -> {
            returns.add(userService.addUser(user1));
            returns.add(userService.addUser(user2));

        });

        // Assert
        assertEquals(expectedUsers, returns);
        assertEquals(expectedUsers, readTestUserData());
    }
    @Test
    public void addUser_WhenUserWithExistingId_ShouldThrowException() throws IOException {
        // Arrange
        UUID existingId = UUID.randomUUID();
        User existingUser = new User(existingId, "Jane Doe", new ArrayList<Order>());
        writeTestUserData(List.of(existingUser));
        User newUser = new User(existingId, "John Doe", new ArrayList<Order>());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.addUser(newUser));
    }
    @Test
    public void addUser_WhenUserIdIsNotNull_ShouldCreateUserWithGivenId()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", new ArrayList<Order>());

        var expectedUsers = List.of(user1, user2);

        // Act
        var return1 = userService.addUser(user1);
        var return2 = userService.addUser(user2);

        var returns = List.of(return1, return2);

        // Assert
        assertEquals(expectedUsers, returns);
        assertEquals(expectedUsers, readTestUserData());
    }

    //if user has orders should be added to orders through order repository if they are not exist
    @Test
    public void addUser_WhenUserHasOrders_ShouldAddOrdersToOrdersRepository() throws IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 20.0);
        var order1 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000101"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 100.0, List.of(product1, product2));
        var order2 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000102"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 200.0, List.of(product1, product2));
        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", List.of(order1, order2));

        var expectedUser = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", List.of(order1, order2));
        var expectedOrders = List.of(order1, order2);

        // Act
        var returnUser = userService.addUser(user);

        // Assert
        assertEquals(expectedUser, returnUser);
        assertEquals(expectedOrders, readTestOrderData());
    }

    //---------------------------
    //GET USERS TEST
    //---------------------------
    @Test
    public void getUsers_WhenUsersExist_ShouldReturnAllUsers() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        var returns = userService.getUsers();

        // Assert
        assertEquals(List.of(user1, user2), returns);
    }
    @Test
    public void getUsers_WhenNoUsersExist_ShouldReturnEmptyList() throws IOException {
        // Arrange
        writeTestUserData(List.of());
        // Act
        var returns = userService.getUsers();

        // Assert
        assertTrue(returns.isEmpty());
    }
    @Test
    public void getUsers_WhenDataFileExistsButContainsInvalidJsonSyntax_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        Files.writeString(getTestUserDataFilePath(), "invalid json");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.getUsers();
        });
    }
    @Test
    public void getUsers_WhenDataFileContainsInvalidUserData_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        Files.writeString(getTestUserDataFilePath(), """
                        [
                            {
                                "id": "00000000-0000-0000-0000-000000000001",
                                "name": 10,
                                "orders": []
                            },
                        ]
                """);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.getUsers();
        });
    }
    @Test
    public void getUsers_WhenDataFileDoesNotExist_ShouldReturnEmptyList() throws IOException {
        // Act
        var returns = userService.getUsers();

        // Assert
        assertTrue(returns.isEmpty());
    }

    //get users when have orders should be fetched correctly from orders repository
    @Test
    public void getUsers_WhenUsersHaveOrders_ShouldReturnUsersWithOrders() throws IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 20.0);
        var order1 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000101"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 100.0, List.of(product1, product2));
        var order2 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000102"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 200.0, List.of(product1, product2));
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", List.of(order1, order2));
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        var returns = userService.getUsers();

        // Assert
        assertEquals(List.of(user1, user2), returns);
    }


    //---------------------------
    //GET USER BY ID TEST
    //---------------------------
    @Test
    public void getUserById_WhenUserExists_ShouldReturnUser() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        var return1 = userService.getUserById(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        var return2 = userService.getUserById(UUID.fromString("00000000-0000-0000-0000-000000000002"));

        // Assert
        assertEquals(user1, return1);
        assertEquals(user2, return2);
    }
    @Test
    public void getUserById_WhenUserDoesNotExist_ShouldReturnNull() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        var return1 = userService.getUserById(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        // Assert
        assertNull(return1);
    }
    @Test
    public void getUserById_WhenNoUsersExist_ShouldReturnNull() throws StreamReadException, DatabindException, IOException {
        // Arrange
        writeTestUserData(List.of());
        // Act
        var returns = userService.getUserById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertNull(returns);
    }
    @Test
    public void getUserById_WhenDataFileDoesNotExist_ShouldReturnNull() throws StreamReadException, DatabindException, IOException {
        //Arrange
        // Act
        var returns = userService.getUserById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertNull(returns);
    }
    @Test
    public void getUserById_WhenDuplicateIdsExist_ShouldReturnFirstUser() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        var returnUser = userService.getUserById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(user1, returnUser);
    }
    //when user has orders its orders should be fetched correctly
    @Test
    public void getUserById_WhenUserHasOrders_ShouldReturnUserWithOrders() throws IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 20.0);
        var order1 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000101"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 100.0, List.of(product1, product2));
        var order2 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000102"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 200.0, List.of(product1, product2));
        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", List.of(order1, order2));

        writeTestUserData(List.of(user));
        writeTestOrderData(List.of(order1, order2));

        // Act
        var returnUser = userService.getUserById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(user, returnUser);
        assertEquals(List.of(order1, order2), returnUser.getOrders());
    }



    //---------------------------
    //DELETE USER TEST
    //---------------------------
    @Test
    public void deleteUserById_WhenUserExists_ShouldDeleteUser() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        userService.deleteUserById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(List.of(user2), readTestUserData());
    }
    @Test
    public void deleteUserById_WhenUserDoesNotExist_ShouldThrowException() throws IOException
    {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.deleteUserById(UUID.fromString("00000000-0000-0000-0000-000000000003")));
    }
    @Test
    public void deleteUserById_WhenNoUsersExist_ShouldThrowException() throws IOException {
        // Arrange
        writeTestUserData(List.of());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.deleteUserById(UUID.fromString("00000000-0000-0000-0000-000000000001")));
    }
    @Test
    public void deleteUserById_WhenDuplicateIdsExist_ShouldDeleteFirstUser() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        userService.deleteUserById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(List.of(user2), readTestUserData());
    }
    //.....................
    //GET THE USER'S OREDERS
    //.....................
    @Test
    public void getOrdersByUserId_WhenUserExists_ShouldReturnUserOrders() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", List.of(
                new Order(UUID.fromString("00000000-0000-0000-0000-000000000101"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 100.0, new ArrayList<Product>()),
                new Order(UUID.fromString("00000000-0000-0000-0000-000000000102"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 200.0, new ArrayList<Product>())
        ));
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", List.of(
                new Order(UUID.fromString("00000000-0000-0000-0000-000000000201"), UUID.fromString("00000000-0000-0000-0000-000000000002"), 300.0, new ArrayList<Product>()),
                new Order(UUID.fromString("00000000-0000-0000-0000-000000000202"), UUID.fromString("00000000-0000-0000-0000-000000000002"), 400.0, new ArrayList<Product>())
        ));

        writeTestUserData(List.of(user1, user2));

        // Act

        var return1 = userService.getOrdersByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        var return2 = userService.getOrdersByUserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));

        System.out.print(return1.get(0).getUserId());
        System.out.print(user1.getOrders().get(0).getUserId());
        // Assert
        assertEquals(user1.getOrders(), return1);
        assertEquals(user2.getOrders(), return2);
    }
   @Test
    public void getOrdersByUserId_WhenUserDoesNotExist_ShouldThrowException() throws IOException {
       // Arrange
       var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());
       var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "user 2", new ArrayList<Order>());

       writeTestUserData(List.of(user1, user2));

       // Act & Assert
       assertThrows(NoSuchElementException.class, () -> userService.getOrdersByUserId(UUID.fromString("00000000-0000-0000-0000-000000000003")));
   }
    @Test
    public void getOrdersByUserId_WhenUserHasNoOrders_ShouldReturnEmptyList() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        var return1 = userService.getOrdersByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        var return2 = userService.getOrdersByUserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));

        // Assert
        assertTrue(return1.isEmpty());
        assertTrue(return2.isEmpty());
    }

    //---------------------------
    // REMOVE ORDER FROM USER TEST
    //---------------------------
    @Test
    public void removeOrderFromUser_WhenUserExistsAndOrderExists_ShouldRemoveOrder() throws IOException {
        // Arrange
        var order1 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000101"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 100.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000102"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 200.0, new ArrayList<Product>());
        var order3 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000103"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 300.0, new ArrayList<Product>());

        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", List.of(order1, order2, order3));
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", List.of(order1, order2, order3));

        writeTestUserData(List.of(user1, user2));

        // Act
        userService.removeOrderFromUser(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000102"));

        // Assert
        assertEquals(List.of(order1, order3), readTestUserData().get(0).getOrders());
    }
    @Test
    public void removeOrderFromUser_WhenUserExistsAndOrderDoesNotExist_ShouldNotRemoveAnyOrder() throws IOException {
        // Arrange
        var order1 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000101"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 100.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000102"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 200.0, new ArrayList<Product>());
        var order3 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000103"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 300.0, new ArrayList<Product>());

        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", List.of(order1, order2, order3));
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", List.of(order1, order2, order3));

        writeTestUserData(List.of(user1, user2));

        // Act
        userService.removeOrderFromUser(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000104"));

        // Assert
        assertEquals(List.of(order1, order2, order3), readTestUserData().get(0).getOrders());
    }
    @Test
    public void removeOrderFromUser_WhenUserDoesNotExist_ShouldThrowException() throws IOException {
        // Arrange
        var order1 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000101"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 100.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000102"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 200.0, new ArrayList<Product>());
        var order3 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000103"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 300.0, new ArrayList<Product>());

        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", List.of(order1, order2, order3));
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", List.of(order1, order2, order3));

        writeTestUserData(List.of(user1, user2));

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.removeOrderFromUser(UUID.fromString("00000000-0000-0000-0000-000000000003"), UUID.fromString("00000000-0000-0000-0000-000000000102")));
    }
    @Test
    public void removeOrderFromUser_WhenNoUsersExist_ShouldThrowException() throws IOException {
        // Arrange
        writeTestUserData(List.of());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.removeOrderFromUser(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000102")));
    }
    @Test
    public void removeOrderFromUser_WhenDuplicateIdsExist_ShouldRemoveOrderFromFirstUser() throws IOException {
        // Arrange
        var order1 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000101"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 100.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000102"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 200.0, new ArrayList<Product>());
        var order3 = new Order(UUID.fromString("00000000-0000-0000-0000-000000000103"), UUID.fromString("00000000-0000-0000-0000-000000000001"), 300.0, new ArrayList<Product>());

        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", List.of(order1, order2, order3));
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 2", List.of(order1, order2, order3));

        writeTestUserData(List.of(user1, user2));

        // Act
        userService.removeOrderFromUser(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000102"));


        // Assert
        assertEquals(List.of(order1, order3), readTestUserData().get(0).getOrders());
    }
    @Test
    public void removeOrderFromUser_WhenUserHasNoOrders_ShouldNotRemoveAnyOrder() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        userService.removeOrderFromUser(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000102"));

        // Assert
        assertEquals(List.of(), readTestUserData().get(0).getOrders());
    }

    // ----------------------------
    //EMPTY CART TEST
    //---------------------------
    @Test
    public void emptyCart_WhenCartExists_ShouldEmptyCart() throws IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 20.0);
        var cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), List.of(product1, product2));
        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());

        writeTestUserData(List.of(user));
        cartService.addCart(cart);

        // Act
        userService.emptyCart(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertTrue(cartService.getCartByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001")).getProducts().isEmpty());
    }
    @Test
    public void emptyCart_WhenUserDoesNotExist_ShouldThrowException() throws IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 20.0);
        var cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), List.of(product1, product2));

        writeTestCartData(List.of(cart));

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.emptyCart(UUID.fromString("00000000-0000-0000-0000-000000000001")));
    }
    @Test
    public void emptyCart_WhenCartIsEmpty_ShouldNotThrowException() throws IOException {
        // Arrange
        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());
       var cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), new ArrayList<Product>());
        writeTestUserData(List.of(user));
        writeTestCartData(List.of(cart));

        // Act
        userService.emptyCart(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertTrue(cartService.getCartByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001")).getProducts().isEmpty());
    }
    @Test
    public void emptyCart_WhenNoUsersExist_ShouldThrowException() throws IOException {
        // Arrange
        writeTestUserData(List.of());
        writeTestCartData(List.of());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.emptyCart(UUID.fromString("00000000-0000-0000-0000-000000000001")));
    }
//    @Test
//    public void emptyCart_WhenDuplicateIdsExist_ShouldEmptyCartForFirstUser() throws IOException {
//        // Arrange
//        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
//        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 20.0);
//        var cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), List.of(product1, product2));
//        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());
//
//        writeTestUserData(List.of(user));
//        cartService.addCart(cart);
//        writeTestCartData(List.of(cart));
//
//
//        // Act
//        userService.emptyCart(UUID.fromString("00000000-0000-0000-0000-000000000001"));
//
//        // Assert
//        assertTrue(cartService.getCartByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001")).getProducts().isEmpty());
//    }
    @Test
    public void emptyCart_WhenCartDoesNotExist_ShouldThrowException() throws IOException {
        // Arrange
        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());
        writeTestUserData(List.of(user));
        writeTestCartData(List.of());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.emptyCart(UUID.fromString("00000000-0000-0000-0000-000000000001")));
    }
    @Test
    public void emptyCart_WhenCartHasOnlyOneItem_ShouldEmptyCart() throws IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
        var cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), List.of(product1));
        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());

        writeTestUserData(List.of(user));

        cartService.addCart(cart);

        // Act
        userService.emptyCart(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertTrue(cartService.getCartByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001")).getProducts().isEmpty());
    }

     //---------------------------
    //ADD ORDER TO USER TEST
    //---------------------------
    @Test
    public void addOrderToUser_WhenUserExistsAndCartIsNotEmpty_ShouldAddOrderToUserAndEmptyCart() throws IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 20.0);
        var cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), List.of(product1, product2));
        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());

        writeTestUserData(List.of(user));

        cartService.addCart(cart);

        // Act
        userService.addOrderToUser(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        var orders = readTestUserData().get(0).getOrders();
        assertTrue(orders.size() == 1);
        assertEquals(30.0, orders.get(0).getTotalPrice());
        //check if orders file has one order added
        assertEquals(1, readTestOrderData().size());
        assertTrue(cartService.getCartByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001")).getProducts().isEmpty());
    }
    @Test
    public void addOrderToUser_WhenUserDoesNotExist_ShouldThrowException() throws IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 20.0);
        var cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), List.of(product1, product2));

        writeTestCartData(List.of(cart));

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.addOrderToUser(UUID.fromString("00000000-0000-0000-0000-000000000001")));
    }
    @Test
    public void addOrderToUser_WhenCartIsEmpty_ShouldThrowException() throws IOException {
        // Arrange
        var cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), new ArrayList<Product>());
        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());

        writeTestUserData(List.of(user));
        writeTestCartData(List.of(cart));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.addOrderToUser(UUID.fromString("00000000-0000-0000-0000-000000000001")));
    }
    @Test
    public void addOrderToUser_WhenCartHasOnlyOneItem_ShouldAddOrderToUserAndEmptyCart() throws IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 10.0);
        var cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), List.of(product1));
        var user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "user 1", new ArrayList<Order>());

        writeTestUserData(List.of(user));

        cartService.addCart(cart);

        // Act
        userService.addOrderToUser(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        var orders = readTestUserData().get(0).getOrders();
        assertTrue(orders.size() == 1);
        assertEquals(10.0, orders.get(0).getTotalPrice());
        assertTrue(cartService.getCartByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001")).getProducts().isEmpty());
    }

}
