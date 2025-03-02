package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.example.model.Product;
import com.example.repository.ProductRepository;
import com.example.utils.TestUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ProductServiceTest {

    // ----------------------------
    // Test setup
    // ----------------------------

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    private Path tempDir;

    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProductServiceTest() {
        productRepository = new ProductRepository() {
            @Override
            protected String getDataPath() {
                return getTestDataFilePath().toString();
            }
        };
        productService = new ProductService(productRepository);
    }

    // ----------------------------
    // Tests
    // ----------------------------

    // ----------------------------
    // addProduct
    // ----------------------------

    @Test
    public void addProduct_WhenProductIdIsNull_ShouldCreateProductWithRandomId()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product("New Product 1", 100.0);
        var product2 = new Product("New Product 2", 200.0);

        var uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

        var expectedProduct1 = new Product(uuid1, "New Product 1", 100.0);
        var expectedProduct2 = new Product(uuid2, "New Product 2", 200.0);

        var expectedProducts = List.of(expectedProduct1, expectedProduct2);

        // Act
        var returns = new ArrayList<Product>();
        TestUtils.withMockedUuids(List.of(uuid1, uuid2), () -> {
            returns.add(productService.addProduct(product1));
            returns.add(productService.addProduct(product2));

        });

        // Assert
        assertEquals(expectedProducts, returns);
        assertEquals(expectedProducts, readTestProductData());
    }

    @Test
    public void addProduct_WhenProductIdIsNotNull_ShouldCreateProductWithGivenId()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "New Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "New Product 2", 200.0);

        var expectedProducts = List.of(product1, product2);

        // Act
        var return1 = productService.addProduct(product1);
        var return2 = productService.addProduct(product2);

        var returns = List.of(return1, return2);

        // Assert
        assertEquals(expectedProducts, returns);
        assertEquals(expectedProducts, readTestProductData());
    }

    @Test
    public void addProduct_WhenProductWithGivenIdAlreadyExists_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var existingProduct = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "New Product 1",
                100.0);
        var newProduct = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "New Product 2", 200.0);

        writeTestProductData(List.of(existingProduct));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productService.addProduct(newProduct);
        });
    }

    // ----------------------------
    // getProducts
    // ----------------------------

    @Test
    public void getProducts_WhenProductsExist_ShouldReturnAllProducts()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);

        writeTestProductData(List.of(product1, product2));

        var expectedProducts = List.of(product1, product2);

        // Act
        var returns = productService.getProducts();

        // Assert
        assertEquals(expectedProducts, returns);
    }

    @Test
    public void getProducts_WhenDataFileDoesNotExist_ShouldReturnEmptyList()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var expectedProducts = List.of();

        // Act
        var returns = productService.getProducts();

        // Assert
        assertEquals(expectedProducts, returns);
    }

    @Test
    public void getProducts_WhenDataFileExistsButIsEmpty_ShouldReturnEmptyList()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        writeTestProductData(List.of());

        var expectedProducts = List.of();

        // Act
        var returns = productService.getProducts();

        // Assert
        assertEquals(expectedProducts, returns);
    }

    @Test
    public void getProducts_WhenDataFileExistsButContainsInvalidJsonSyntax_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        Files.writeString(getTestDataFilePath(), "invalid json");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.getProducts();
        });
    }

    @Test
    public void getProducts_WhenDataFileContainsInvalidProductData_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        Files.writeString(getTestDataFilePath(), """
                        [
                            {
                                "id": "00000000-0000-0000-0000-000000000001",
                                "name": 10,
                                "price": 100.0
                            },
                        ]
                """);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.getProducts();
        });
    }

    // ----------------------------
    // getProductById
    // ----------------------------

    @Test
    public void getProductById_WhenProductExists_ShouldReturnProduct()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);

        writeTestProductData(List.of(product1, product2));

        var expectedProduct = product2;

        // Act
        var returnProduct = productService.getProductById(product2.getId());

        // Assert
        assertEquals(expectedProduct, returnProduct);
    }

    @Test
    public void getProductById_WhenProductDoesNotExist_ShouldReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);

        writeTestProductData(List.of(product1, product2));

        // Act
        var returnProduct = productService.getProductById(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        // Assert
        assertEquals(null, returnProduct);
    }

    @Test
    public void getProductById_WhenDataFileIsEmpty_ShouldReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        writeTestProductData(List.of());

        // Act
        var returnProduct = productService.getProductById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(null, returnProduct);
    }

    @Test
    public void getProductById_WhenDataFileDoesNotExist_ShouldReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange

        // Act
        var returnProduct = productService.getProductById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(null, returnProduct);
    }

    @Test
    public void getProductById_WhenDuplicateProductIdsExist_ShouldReturnFirstProduct()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product ", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        var expectedProduct = product3;

        // Act
        var returnProduct = productService.getProductById(product3.getId());

        // Assert
        assertEquals(expectedProduct, returnProduct);
    }

    // ----------------------------
    // updateProduct
    // ----------------------------

    @Test
    public void updateProduct_WhenProductExists_ShouldUpdateProduct()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);

        writeTestProductData(List.of(product1, product2));

        var expectedProduct = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "New Product 2",
                123.0);

        // Act
        var returnProduct = productService.updateProduct(product2.getId(), "New Product 2", 123.0);

        // Assert
        assertEquals(expectedProduct, returnProduct);
        assertEquals(List.of(product1, expectedProduct), readTestProductData());
    }

    @Test
    public void updateProduct_WhenProductDoesNotExist_ShouldIgnoreUpdateAndReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);

        writeTestProductData(List.of(product1, product2));

        // Act
        var returnProduct = productService.updateProduct(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "New Product 2", 123.0);

        // Assert
        assertEquals(null, returnProduct);
        assertEquals(List.of(product1, product2), readTestProductData());
    }

    @Test
    public void updateProduct_WhenDuplicateProductIdsExist_ShouldUpdateFirstProduct()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 4", 400.0);

        var expectedProduct = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "New Product 3",
                123.0);

        var products = List.of(product1, product2, product3, product4);
        var expectedProducts = List.of(product1, product2, expectedProduct, product4);

        writeTestProductData(products);

        // Act
        var returnProduct = productService.updateProduct(product3.getId(), "New Product 3", 123.0);

        // Assert
        assertEquals(expectedProduct, returnProduct);
        assertEquals(expectedProducts, readTestProductData());
    }

    // ----------------------------
    // deleteProductById
    // ----------------------------

    @Test
    public void deleteProduct_WhenProductExists_ShouldDeleteProduct()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);

        writeTestProductData(List.of(product1, product2));

        // Act
        productService.deleteProductById(product2.getId());

        // Assert
        assertEquals(List.of(product1), readTestProductData());
    }

    @Test
    public void deleteProduct_WhenProductDoesNotExist_ShouldIgnoreDeleteAndReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);

        writeTestProductData(List.of(product1, product2));

        // Act
        productService.deleteProductById(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        // Assert
        assertEquals(List.of(product1, product2), readTestProductData());
    }

    @Test
    public void deleteProduct_WhenNoProductsExist_ShouldIgnoreDeleteAndReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        writeTestProductData(List.of());

        // Act
        productService.deleteProductById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // Assert
        assertEquals(List.of(), readTestProductData());
    }

    // ----------------------------
    // applyDiscount
    // ----------------------------

    @Test
    public void applyDiscount_WhenApplyingOnSingleExistingProduct_ShouldApplyDiscountToProduct()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        // Act
        productService.applyDiscount(0.1, new ArrayList<>(List.of(product2.getId())));
        var expectedProduct = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 180.0);

        // Assert
        assertEquals(List.of(product1, expectedProduct, product3, product4), readTestProductData());
    }

    @Test
    public void applyDiscount_WhenApplyingOnMultipleExistingProducts_ShouldApplyDiscountToProducts()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        // Act
        productService.applyDiscount(0.2, new ArrayList<>(List.of(product1.getId(), product3.getId())));
        var expectedProduct1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 80.0);
        var expectedProduct3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 240.0);

        // Assert
        assertEquals(List.of(expectedProduct1, product2, expectedProduct3, product4), readTestProductData());
    }

    @Test
    public void applyDiscount_WhenApplyingOnNonExistingProduct_ShouldIgnoreDiscountAndReturnNull()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        // Act
        productService.applyDiscount(0.1,
                new ArrayList<>(List.of(UUID.fromString("00000000-0000-0000-0000-000000000005"))));

        // Assert
        assertEquals(List.of(product1, product2, product3, product4), readTestProductData());
    }

    @Test
    public void applyDiscount_WhenApplyingOnSomeExistingProductsAndSomeNonExisting_ShouldApplyDiscountToExistingProducts()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        // Act
        productService.applyDiscount(0.1,
                new ArrayList<>(List.of(product1.getId(), UUID.fromString("00000000-0000-0000-0000-000000000005"))));
        var expectedProduct1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 90.0);

        // Assert
        assertEquals(List.of(expectedProduct1, product2, product3, product4), readTestProductData());
    }

    @Test
    public void applyDiscount_WhenApplyingOnNoProducts_ShouldIgnoreDiscount()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        // Act
        productService.applyDiscount(0.1, new ArrayList<>());

        // Assert
        assertEquals(List.of(product1, product2, product3, product4), readTestProductData());
    }
    
    @Test
    public void applyDiscount_WhenApplyingAZeroDiscount_ShouldIgnoreDiscount()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        // Act
        productService.applyDiscount(0.0, new ArrayList<>(List.of(product1.getId(), product2.getId(), product3.getId(), product4.getId())));

        // Assert
        assertEquals(List.of(product1, product2, product3, product4), readTestProductData());
    }
    
    @Test
    public void applyDiscount_WhenApplyingA100PercentDiscount_ShouldSetPriceToZero()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        // Act
        productService.applyDiscount(1.0, new ArrayList<>(List.of(product1.getId(), product2.getId(), product3.getId())));
        var expectedProduct1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 0.0);
        var expectedProduct2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 0.0);
        var expectedProduct3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 0.0);

        // Assert
        assertEquals(List.of(expectedProduct1, expectedProduct2, expectedProduct3, product4), readTestProductData());
    }
    
    @Test
    public void applyDiscount_WhenApplyingANegativeDiscount_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productService.applyDiscount(-0.1, new ArrayList<>(List.of(product1.getId(), product2.getId(), product3.getId())));
        });
    }
    
    @Test
    public void applyDiscount_WhenApplyingADiscountGreaterThanOne_ShouldThrowException()
            throws StreamReadException, DatabindException, IOException {
        // Arrange
        var product1 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Product 1", 100.0);
        var product2 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Product 2", 200.0);
        var product3 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Product 3", 300.0);
        var product4 = new Product(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Product 4", 400.0);

        writeTestProductData(List.of(product1, product2, product3, product4));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productService.applyDiscount(1.1, new ArrayList<>(List.of(product1.getId(), product2.getId(), product3.getId())));
        });
    }

    // ----------------------------
    // Helper methods
    // ----------------------------

    private List<Product> readTestProductData() throws StreamReadException, DatabindException, IOException {
        return objectMapper.readValue(getTestDataFilePath().toFile(), new TypeReference<List<Product>>() {
        });
    }

    private void writeTestProductData(List<Product> products) throws IOException {
        objectMapper.writeValue(getTestDataFilePath().toFile(), products);
    }

    private Path getTestDataFilePath() {
        return tempDir.resolve("products.json");
    }

}
