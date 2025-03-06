package com.example.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.User;
import com.example.repository.CartRepository;
import com.example.repository.UserRepository;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.example.model.Product;
import com.example.repository.ProductRepository;
import com.example.utils.TestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
//cart service test cases
public class CartServiceTest {
    // ----------------------------
    // Test setup
    // ----------------------------

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    private Path tempDir;  // JUnit initializes this before test methods, not before constructor

    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;
    private CartService cartService;

    @BeforeEach
    public void setUp() {
        productRepository = new ProductRepository() {
            @Override
            protected String getDataPath() {
                return getTestDataFilePath("products.json").toString();
            }
        };

        cartRepository = new CartRepository() {
            @Override
            protected String getDataPath() {
                return getTestDataFilePath("carts.json").toString();
            }
        };

        userRepository = new UserRepository() {
            @Override
            protected String getDataPath() {
                return getTestDataFilePath("users.json").toString();
            }
        };

        cartService = new CartService(cartRepository, productRepository, userRepository);


    }

    // --------------------------
    // Tests
    // --------------------------

    //----------
    //addCart
    //-------
    @Test
    public void addCart_WhenUserAlreadyHasCart_ShouldThrowException() throws IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        userRepository.addUser(user1);

        var existingCart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000002"), user1.getId());
        writeTestCartData(List.of(existingCart)); // Ensure cart already exists

        var newCart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000003"), user1.getId());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cartService.addCart(newCart));
    }

    @Test
    public void addCart_WhenUserHasNoCart_ShouldCreateNewCart() throws IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        userRepository.addUser(user1);

        var uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000002"); // Random ID for new cart
        Cart newCart = new Cart(user1.getId()); // Cart with no predefined ID

        var expectedCart = new Cart(uuid1, user1.getId());
        var expectedCarts = List.of(expectedCart);

        // Act
        var returns = new ArrayList<Cart>();
        TestUtils.withMockedUuids(List.of(uuid1), () -> {
            returns.add(cartService.addCart(newCart));
        });

        List<Cart> actualCarts = readTestCartData(); // Read stored carts

        // Assert
        assertNotNull(returns.get(0));
        assertEquals(user1.getId(), returns.get(0).getUserId()); // Ensure correct user ID
        assertEquals(expectedCart.getId(), returns.get(0).getId()); // Ensure ID is set correctly
        assertIterableEquals(expectedCarts, readTestCartData());
    }

    @Test
    public void addCart_WhenCartIdIsNull_ShouldCreateCartWithRandomId() throws IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        User user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test User2", new ArrayList<>());

        userRepository.addUser(user1);
        userRepository.addUser(user2);

        Cart cart1 = new Cart(user1.getId());
        Cart cart2 = new Cart(user2.getId());

        var uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

        var expectedCart1 = new Cart(uuid1, user1.getId());
        var expectedCart2 = new Cart(uuid2, user2.getId());

        var expectedCarts = List.of(expectedCart1, expectedCart2);

        // Act
        var returns = new ArrayList<Cart>();
        TestUtils.withMockedUuids(List.of(uuid1, uuid2), () -> {
            returns.add(cartService.addCart(cart1));
            returns.add(cartService.addCart(cart2));
        });

        // Assert
        assertEquals(expectedCarts, returns);
        assertEquals(expectedCarts, readTestCartData());
    }

    @Test
    public void addCart_WhenCartIdIsGiven_ShouldCreateCartWithGivenId() throws IOException {

        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        User user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test User2", new ArrayList<>());

        userRepository.addUser(user1);
        userRepository.addUser(user2);

        Cart cart1 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        Cart cart2 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000002"), user2.getId());

        var expectedCarts = List.of(cart1, cart2);

        // Act
        var returns = new ArrayList<Cart>();
        returns.add(cartService.addCart(cart1));
        returns.add(cartService.addCart(cart2));

        // Assert
        assertEquals(expectedCarts, returns);
        assertEquals(expectedCarts, readTestCartData());
    }

    @Test
    public void addCart_WhenCartWithGivenIdAlreadyExists_ShouldThrowException() throws IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        User user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test User2", new ArrayList<>());

        userRepository.addUser(user1);
        userRepository.addUser(user2);

        var existingCart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        var newCart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user2.getId());

        writeTestCartData(List.of(existingCart));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartService.addCart(newCart);
        });
    }
    @Test
    public void addCart_WhenUserIdIsInvalid_ShouldThrowException() {
        // Arrange
        Cart cart = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> cartService.addCart(cart));
    }
    @Test
    public void addCart_WhenUserIdIsValid_ShouldReturnCartWithGivenUserId() throws IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        userRepository.addUser(user1);

        var uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");

        Cart cart1 = new Cart(user1.getId()); // Cart without predefined ID

        var expectedCart1 = new Cart(uuid1, user1.getId());
        var expectedCarts = List.of(expectedCart1);

        // Act
        var returns = new ArrayList<Cart>();
        TestUtils.withMockedUuids(List.of(uuid1), () -> {
            returns.add(cartService.addCart(cart1));
        });

        List<Cart> actualCarts = readTestCartData(); // Read stored carts

        // Assert
        assertNotNull(returns.get(0));
        assertEquals(user1.getId(), returns.get(0).getUserId()); // Ensure correct user ID
        assertEquals(expectedCart1.getId(), returns.get(0).getId()); // Ensure ID is set correctly
        assertIterableEquals(expectedCarts, readTestCartData());
    }



    //------------------------
    // Get All Carts
    //--------------------------
    @Test
    public void getCarts_WhenCartsExist_ShouldReturnAllCarts()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        User user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test User2", new ArrayList<>());

        userRepository.addUser(user1);
        userRepository.addUser(user2);
        Cart cart1 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        Cart cart2 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000002"), user2.getId());

        writeTestCartData(List.of(cart1, cart2));

        var expectedCarts = List.of(cart1, cart2);

        // Act
        var returns = cartService.getCarts();

        // Assert
        assertEquals(expectedCarts, returns);
    }
    @Test
    public void getCarts_WhenDataFileDoesNotExist_ShouldReturnEmptyList()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var expectedCarts = List.of();

        // Act
        var returns = cartService.getCarts();

        // Assert
        assertEquals(expectedCarts, returns);
    }
    @Test
    public void getCarts_WhenDataFileExistsButIsEmpty_ShouldReturnEmptyList()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        writeTestCartData(List.of());

        var expectedCarts = List.of();

        // Act
        var returns = cartService.getCarts();

        // Assert
        assertEquals(expectedCarts, returns);
    }
    @Test
    public void getCarts_WhenDataFileExistsButContainsInvalidJsonSyntax_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        Files.writeString(getTestDataFilePath("carts.json"), "invalid json");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cartService.getCarts();
        });
    }
    @Test
    public void getCarts_WhenDataFileContainsInvalidCartData_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        Files.writeString(getTestDataFilePath("carts.json"), """
                        [
                            {
                                "id": "00000000-0000-0000-0000-000000000001",
                                "userId": "00000000-0000-0000-0000-000000000001",
                                "products": []
                            },
                        ]
                """);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cartService.getCarts();
        });
    }


    //------------------------
    // getCartById
    //--------------------------

    @Test
    public void getCartById_WhenCartExists_ShouldReturnCart()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        User user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test User2", new ArrayList<>());

        userRepository.addUser(user1);
        userRepository.addUser(user2);
        var cart1 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        var cart2 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000002"), user2.getId());

        writeTestCartData(List.of(cart1, cart2));

        var expectedCart = cart2;

        // Act
        var returnCart = cartService.getCartById(cart2.getId());

        // Assert
        assertEquals(expectedCart, returnCart);
    }

    @Test
    public void getCartById_WhenCartDoesNotExist_ShouldReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        User user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test User2", new ArrayList<>());

        userRepository.addUser(user1);
        userRepository.addUser(user2);
        var cart1 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        var cart2 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000002"), user2.getId());

        writeTestCartData(List.of(cart1, cart2));

        // Act
        var returnCart = cartService.getCartById(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        // Assert
        assertEquals(null, returnCart);
    }

    @Test
    public void getCartById_WhenDataFileIsEmpty_ShouldReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        writeTestCartData(List.of());

        // Act
        var returnCart = cartService.getCartById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(null, returnCart);
    }

    @Test
    public void getCartById_WhenDataFileDoesNotExist_ShouldReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange

        // Act
        var returnCart = cartService.getCartById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(null, returnCart);
    }

    @Test
    public void getCartById_WhenDuplicateCartIdsExist_ShouldReturnFirstCart()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        User user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test User2", new ArrayList<>());
        User user3 = new User(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Test User3", new ArrayList<>());


        userRepository.addUser(user1);
        userRepository.addUser(user2);
        userRepository.addUser(user3);
        var cart1 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        var cart2 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000002"), user2.getId());
        var cart3 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user3.getId());

        writeTestCartData(List.of(cart1, cart2, cart3));

        var expectedCart = cart1;

        // Act
        var returnCart = cartService.getCartById(cart1.getId());

        // Assert
        assertEquals(expectedCart, returnCart);
    }
    // ----------------------------
    // getCartByUserId
    // ----------------------------

    @Test
    public void getCartByUserId_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        UUID nonExistentUserId = UUID.fromString("00000000-0000-0000-0000-000000000999");

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            cartService.getCartByUserId(nonExistentUserId);
        });
    }

    @Test
    public void getCartByUserId_WhenUserHasNoCart_ShouldCreateNewCart() throws IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        userRepository.addUser(user1);

        var uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");

        var expectedCart = new Cart(uuid1, user1.getId());

        // Act
        var returns = new ArrayList<Cart>();
        TestUtils.withMockedUuids(List.of(uuid1), () -> {
            returns.add(cartService.getCartByUserId(user1.getId()));
        });

        List<Cart> actualCarts = readTestCartData(); // Read stored carts

        // Assert
        assertNotNull(returns.get(0));
        assertEquals(expectedCart.getUserId(), returns.get(0).getUserId()); // Ensure correct user ID
        assertEquals(0, returns.get(0).getProducts().size()); // Ensure cart is empty
        assertIterableEquals(List.of(expectedCart), actualCarts);
    }

    @Test
    public void getCartByUserId_WhenUserHasCart_ShouldReturnCart() throws IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        userRepository.addUser(user1);

        var cart1 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        writeTestCartData(List.of(cart1));

        var expectedCart = cart1;

        // Act
        var returnCart = cartService.getCartByUserId(user1.getId());

        // Assert
        assertEquals(expectedCart, returnCart);
    }
    // ----------------------------
    // addProductToCart
    // ----------------------------

    @Test
    public void addProductToCart_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        userRepository.addUser(user1);
        var cart1 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        Product nonExistentProduct = new Product(UUID.fromString("00000000-0000-0000-0000-000000000999"), "Nonexistent Product", 100.0);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            cartService.addProductToCart(cart1.getId(), nonExistentProduct);
        });
    }

    @Test
    public void addProductToCart_WhenCartDoesNotExist_ShouldThrowException() {
        // Arrange
        UUID nonExistentCartId = UUID.fromString("00000000-0000-0000-0000-000000000999");
        Product product = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test Product", 50.0);

        productRepository.addProduct(product); // Ensure the product exists

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            cartService.addProductToCart(nonExistentCartId, product);
        });
    }

    @Test
    public void addProductToCart_WhenCartAndProductExist_ShouldAddProduct() throws IOException {
        // Arrange
        User user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User", new ArrayList<>());
        userRepository.addUser(user);

        UUID cartId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Cart cart = new Cart(cartId, user.getId());
        writeTestCartData(List.of(cart)); // Ensure cart exists in the JSON file

        Product product = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test Product", 50.0);
        productRepository.addProduct(product); // Ensure product exists

        // Act
        cartService.addProductToCart(cartId, product);

        // Assert
        Cart updatedCart = cartService.getCartById(cartId);
        assertTrue(updatedCart.getProducts().contains(product));
    }

    @Test
    public void addProductToCart_WhenProductAlreadyInCart_ShouldThrowException() throws IOException {
        // Arrange
        User user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User", new ArrayList<>());
        userRepository.addUser(user);

        UUID cartId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Cart cart = new Cart(cartId, user.getId());

        Product product = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test Product", 50.0);
        productRepository.addProduct(product);

        // Add product once
        cart.getProducts().add(product);
        writeTestCartData(List.of(cart)); // Save the cart with the product already inside

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cartService.addProductToCart(cartId, product));
    }

    // ----------------------------
    // deleteProductFromCart
    // ----------------------------

    @Test
    public void deleteProductFromCart_WhenCartDoesNotExist_ShouldThrowException() {
        // Arrange
        UUID nonExistentCartId = UUID.fromString("00000000-0000-0000-0000-000000000999");
        Product product = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test Product", 50.0);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            cartService.deleteProductFromCart(nonExistentCartId, product);
        });
    }

    @Test
    public void deleteProductFromCart_WhenProductNotInCart_ShouldDoNothing() throws IOException {
        // Arrange
        User user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User", new ArrayList<>());
        userRepository.addUser(user);

        UUID cartId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Cart cart = new Cart(cartId, user.getId());
        writeTestCartData(List.of(cart)); // Cart exists but is empty

        Product product = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test Product", 50.0);

        // Act
        cartService.deleteProductFromCart(cartId, product);

        // Assert
        Cart updatedCart = cartService.getCartById(cartId);
        assertTrue(updatedCart.getProducts().isEmpty()); // No change
    }

    @Test
    public void deleteProductFromCart_WhenProductInCart_ShouldRemoveProduct() throws IOException {
        // Arrange
        User user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User", new ArrayList<>());
        userRepository.addUser(user);

        UUID cartId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Product product = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test Product", 50.0);
        productRepository.addProduct(product);

        Cart cart = new Cart(cartId, user.getId());
        cart.getProducts().add(product);
        writeTestCartData(List.of(cart)); // Save the cart with the product

        // Act
        cartService.deleteProductFromCart(cartId, product);

        // Assert
        Cart updatedCart = cartService.getCartById(cartId);
        assertFalse(updatedCart.getProducts().contains(product)); // Product is removed
    }
    @Test
    public void deleteProductFromCart_WhenProductDoesNotExist_ShouldDoNothing() throws IOException {
        // Arrange
        User user = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User", new ArrayList<>());
        userRepository.addUser(user);

        UUID cartId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Cart cart = new Cart(cartId, user.getId());

        Product existingProduct = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Existing Product", 50.0);
        Product nonExistentProduct = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Non-Existent Product", 60.0);

        productRepository.addProduct(existingProduct);
        cart.getProducts().add(existingProduct); // Cart has one product
        writeTestCartData(List.of(cart)); // Save the cart with only the existing product

        // Act
        cartService.deleteProductFromCart(cartId, nonExistentProduct); // Attempt to delete a product that is not in the cart

        // Assert
        Cart updatedCart = cartService.getCartById(cartId);
        assertTrue(updatedCart.getProducts().contains(existingProduct)); // Ensure the existing product is still in the cart
        assertEquals(1, updatedCart.getProducts().size()); // Cart should still have 1 product
    }
    //------------------------
    //deleteCart
    //------------------------

    @Test
    public void deleteCart_WhenCartExists_ShouldDeleteCart()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        User user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test User2", new ArrayList<>());

        userRepository.addUser(user1);
        userRepository.addUser(user2);
        var cart1 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        var cart2 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000002"), user2.getId());

        writeTestCartData(List.of(cart1, cart2));

        // Act
        cartService.deleteCartById(cart2.getId());

        // Assert
        assertEquals(List.of(cart1), readTestCartData());
    }

    @Test
    public void deleteCart_WhenProductDoesNotExist_ShouldIgnoreDeleteAndReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        User user1 = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User1", new ArrayList<>());
        User user2 = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Test User2", new ArrayList<>());

        userRepository.addUser(user1);
        userRepository.addUser(user2);
        var cart1 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000001"), user1.getId());
        var cart2 = new Cart(UUID.fromString("00000000-0000-0000-0000-000000000002"), user2.getId());

        writeTestCartData(List.of(cart1, cart2));

        // Act
        cartService.deleteCartById(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        // Assert
        assertEquals(List.of(cart1, cart2), readTestCartData());
    }

    @Test
    public void deleteCart_WhenNoCartsExist_ShouldIgnoreDeleteAndReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        writeTestCartData(List.of());

        // Act
        cartService.deleteCartById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(List.of(), readTestCartData());
    }


    // ----------------------------
    // Helper Methods
    // ----------------------------

    private List<Cart> readTestCartData() throws IOException {
        return objectMapper.readValue(getTestDataFilePath("carts.json").toFile(), new TypeReference<List<Cart>>() {});
    }

    private void writeTestCartData(List<Cart> carts) throws IOException {
        objectMapper.writeValue(getTestDataFilePath("carts.json").toFile(), carts);
    }

    private Path getTestDataFilePath(String fileName) {
        return tempDir.resolve(fileName);
    }
}
