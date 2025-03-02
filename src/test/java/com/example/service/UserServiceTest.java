package com.example.service;


import com.example.model.Order;
import com.example.model.Product;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.utils.TestUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    private Path tempDir;

    private final UserRepository userRepository;
    private final UserService userService;


    public UserServiceTest() {
        userRepository = new UserRepository() {
            @Override
            protected String getDataPath() {
                return getTestDataFilePath().toString();
            }
        };

        userService = new UserService(userRepository,new CartService(),new OrderService());
    }

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
        Files.writeString(getTestDataFilePath(), "invalid json");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.getUsers();
        });
    }
    @Test
    public void getUsers_WhenDataFileContainsInvalidUserData_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        Files.writeString(getTestDataFilePath(), """
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
    public void deleteUserById_WhenUserDoesNotExist_ShouldNotDeleteAnyUser() throws IOException {
        // Arrange
        var user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),"user 1", new ArrayList<Order>());
        var user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),"user 2", new ArrayList<Order>());

        writeTestUserData(List.of(user1, user2));

        // Act
        userService.deleteUserById(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        // Assert
        assertEquals(List.of(user1, user2), readTestUserData());
    }
    @Test
    public void deleteUserById_WhenNoUsersExist_ShouldNotDeleteAnyUser() throws IOException {
        // Arrange
        writeTestUserData(List.of());

        // Act
        userService.deleteUserById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertTrue(readTestUserData().isEmpty());
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
    }//GET THE USER'S OREDERS
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
   //1)successful trial
    //2)user doesnt exist throw no such ..
    //3) cart already empty
    //4)no users exist
    //5)duplicate ids
    //6)no cart for this user throw no such ..
    //7)cart has only 1 item

     //---------------------------
    //ADD ORDER TO USER TEST
    //---------------------------
    //1)successful trial
    //2)user doesnt exist throw no such ..
    //3)cart is empty throw exception or return without creating an order
    //4)








    // ----------------------------
    // Helper methods
    // ----------------------------
    private List<User> readTestUserData() throws IOException {
        return objectMapper.readValue(getTestDataFilePath().toFile(), new TypeReference<List<User>>() {});
    }

    private void writeTestUserData(List<User> users) throws IOException {
        objectMapper.writeValue(getTestDataFilePath().toFile(), users);
    }

    private Path getTestDataFilePath() {
        return tempDir.resolve("users.json");
    }

}

