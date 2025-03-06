package com.example.service;

import com.example.model.Order;
import com.example.model.Product;
import com.example.repository.OrderRepository;
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


public class OrderServiceTest {
    // ----------------------------
    // Test setup
    // ----------------------------
    @TempDir
    private Path tempDir;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OrderRepository orderRepository;
    private final OrderService orderService;



    public OrderServiceTest() {
        this.orderRepository = new OrderRepository() {
            @Override
            protected String getDataPath() {
                return getTestDataFilePath().toString();
            }
        };

        this.orderService = new OrderService(orderRepository);
    }
    private Path getTestDataFilePath() {
        return tempDir.resolve("orders.json");
    }

    private List<Order> readTestOrderData() throws StreamReadException, DatabindException, IOException {
        return objectMapper.readValue(getTestDataFilePath().toFile(), new TypeReference<List<Order>>() {
        });
    }
    private void writeTestOrderData(List<Order> orders) throws IOException {
        objectMapper.writeValue(getTestDataFilePath().toFile(), orders);
    }

    // ----------------------------
    // Tests
    // ----------------------------

    // ----------------------------
    // addOrder
    // ----------------------------

    @Test
    public void addOrder_WhenOrderIdIsNull_ShouldCreateOrderWithRandomId()
            throws IOException {
       //ARRANGE
        var userId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        var order1 = new Order(userId, 1550.0, new ArrayList<Product>());
        var order2 = new Order(userId, 2500.0, new ArrayList<Product>());

        var uuid1 = UUID.fromString("10000000-0000-0000-0000-000000000001");
        var uuid2 = UUID.fromString("20000000-0000-0000-0000-000000000002");

        var expectedOrders = List.of(
                new Order(uuid1, userId, 1550.0, new ArrayList<Product>()),
                new Order(uuid2, userId, 2500.0, new ArrayList<Product>())
        );

       //ACT
        TestUtils.withMockedUuids(List.of(uuid1, uuid2), () -> {
            orderService.addOrder(order1);
            orderService.addOrder(order2);
        });

        // Fetch the actual orders from the repository
        var actualOrders = orderService.getOrders();

       //ASSERT
        assertEquals(expectedOrders.size(), actualOrders.size()); // Ensure same number of orders
        assertTrue(actualOrders.containsAll(expectedOrders)); // Ensure all expected orders exist
        assertEquals(expectedOrders, readTestOrderData()); // Verify storage consistency
    }

    @Test
    public void addOrder_WhenOrderIdIsNotNull_ShouldCreateOrderWithGivenId() throws IOException {
        // Arrange
        var userId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        var order1 = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 1550.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("20000000-0000-0000-0000-000000000002"), userId, 2500.0, new ArrayList<Product>());

        var expectedOrders = List.of(order1, order2);

        // Act
        orderService.addOrder(order1);
        orderService.addOrder(order2);

        // Fetch actual stored orders
        var actualOrders = orderService.getOrders();

        // Assert
        assertEquals(expectedOrders.size(), actualOrders.size()); // Ensure same number of orders
        assertTrue(actualOrders.containsAll(expectedOrders)); // Ensure expected orders exist
        assertEquals(expectedOrders, readTestOrderData()); // Verify data persistence
    }
    @Test
    public void addOrder_WhenOrderWithGivenIdAlreadyExists_ShouldThrowException() throws IOException {
        // Arrange
        var userId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        var existingOrder = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 1550.0, new ArrayList<Product>());
        var newOrder = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 2500.0, new ArrayList<Product>());

        writeTestOrderData(List.of(existingOrder));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.addOrder(newOrder);
        });
    }

    // ----------------------------
    // getOrders
    // ----------------------------


    @Test
    public void getOrders_WhenOrdersExist_ShouldReturnAllOrders() throws IOException {
        // Arrange
        var userId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        var order1 = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 1550.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("20000000-0000-0000-0000-000000000002"), userId, 2500.0, new ArrayList<Product>());

        writeTestOrderData(List.of(order1, order2));

        var expectedOrders = List.of(order1, order2);

        // Act
        var actualOrders = orderService.getOrders();

        // Assert
        assertEquals(expectedOrders, actualOrders);
    }

    @Test
    public void getOrders_WhenDataFileDoesNotExist_ShouldReturnEmptyList() throws IOException {
        // Arrange
        var expectedOrders = List.of();

        // Act
        var actualOrders = orderService.getOrders();

        // Assert
        assertEquals(expectedOrders, actualOrders);
    }

    @Test
    public void getOrders_WhenDataFileExistsButIsEmpty_ShouldReturnEmptyList() throws IOException {
        // Arrange
        writeTestOrderData(List.of());

        var expectedOrders = List.of();

        // Act
        var actualOrders = orderService.getOrders();

        // Assert
        assertEquals(expectedOrders, actualOrders);
    }

    @Test
    public void getOrders_WhenDataFileExistsButContainsInvalidJsonSyntax_ShouldThrowException() throws IOException {
        // Arrange
        Files.writeString(getTestDataFilePath(), "invalid json");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrders();
        });
    }
    @Test
    public void getOrders_WhenDataFileContainsInvalidOrderData_ShouldThrowException() throws IOException {
        // Arrange
        Files.writeString(getTestDataFilePath(), """
                    [
                        {
                            "id": "10000000-0000-0000-0000-000000000001",
                            "userId": 12345,
                            "totalPrice": "invalid",
                            "products": []
                        }
                    ]
            """);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrders();
        });
    }
    // ----------------------------
    // getOrderById
    // ----------------------------


    @Test
    public void getOrderById_WhenOrderExists_ShouldReturnOrder() throws IOException {
        // Arrange
        var userId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        var order1 = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 1550.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("20000000-0000-0000-0000-000000000002"), userId, 2500.0, new ArrayList<Product>());

        writeTestOrderData(List.of(order1, order2));

        var expectedOrder = order2;

        // Act
        var returnOrder = orderService.getOrderById(order2.getId());

        // Assert
        assertEquals(expectedOrder, returnOrder);
    }
    @Test
    public void getOrderById_WhenOrderDoesNotExist_ShouldReturnNull() throws IOException {
        // Arrange
        var userId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        var order1 = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 1550.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("20000000-0000-0000-0000-000000000002"), userId, 2500.0, new ArrayList<Product>());

        writeTestOrderData(List.of(order1, order2));

        // Act
        var returnOrder = orderService.getOrderById(UUID.fromString("30000000-0000-0000-0000-000000000003"));

        // Assert
        assertEquals(null, returnOrder);
    }

    @Test
    public void getOrderById_WhenDataFileIsEmpty_ShouldReturnNull() throws IOException {
        // Arrange
        writeTestOrderData(List.of());

        // Act
        var returnOrder = orderService.getOrderById(UUID.fromString("10000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(null, returnOrder);
    }
    @Test
    public void getOrderById_WhenDataFileDoesNotExist_ShouldReturnNull() throws IOException {


        // Act
        var returnOrder = orderService.getOrderById(UUID.fromString("10000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(null, returnOrder);
    }
    @Test
    public void getOrderById_WhenDuplicateOrderIdsExist_ShouldReturnFirstOrder() throws IOException {
        // Arrange
        var userId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        var order1 = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 1550.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 2500.0, new ArrayList<Product>());
        var order3 = new Order(UUID.fromString("20000000-0000-0000-0000-000000000002"), userId, 3000.0, new ArrayList<Product>());
        var order4 = new Order(UUID.fromString("20000000-0000-0000-0000-000000000002"), userId, 4000.0, new ArrayList<Product>());

        writeTestOrderData(List.of(order1, order2, order3, order4));

        var expectedOrder = order3; // Expecting first match

        // Act
        var returnOrder = orderService.getOrderById(order3.getId());

        // Assert
        assertEquals(expectedOrder, returnOrder);
    }

    // ----------------------------
    // deleteOrderById
    // ----------------------------

    @Test
    public void deleteOrder_WhenOrderExists_ShouldDeleteOrder() throws IOException {
        // Arrange
        var userId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        var order1 = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 1550.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("20000000-0000-0000-0000-000000000002"), userId, 2500.0, new ArrayList<Product>());

        writeTestOrderData(List.of(order1, order2));

        // Act
        orderService.deleteOrderById(order2.getId());

        // Assert
        assertEquals(List.of(order1), readTestOrderData()); // Only order1 should remain
    }
    @Test
    public void deleteOrder_WhenOrderDoesNotExist_ShouldThrowException() throws IOException{
        // Arrange
        var userId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        var order1 = new Order(UUID.fromString("10000000-0000-0000-0000-000000000001"), userId, 1550.0, new ArrayList<Product>());
        var order2 = new Order(UUID.fromString("20000000-0000-0000-0000-000000000002"), userId, 2500.0, new ArrayList<Product>());

        writeTestOrderData(List.of(order1, order2));

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            orderService.deleteOrderById(UUID.fromString("30000000-0000-0000-0000-000000000003"));
        });
    }
    @Test
    public void deleteOrder_WhenNoOrdersExist_ShouldThrowException() throws IOException {
        // Arrange
        writeTestOrderData(List.of()); // Empty order data

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            orderService.deleteOrderById(UUID.fromString("0000000-0000-0000-0000-000000000003"));
        });
    }



}
