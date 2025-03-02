package com.example.repository;

import com.example.model.Product;

import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository


public class ProductRepository extends MainRepository<Product>{
    public ProductRepository() {
    }
    @Override
    protected String getDataPath() {
        return "src\\main\\java\\com\\example\\data\\products.json";
    }

    @Override
    protected Class<Product[]> getArrayType() {
        return Product[].class;
    }
    public Product getProductById(UUID productId){
        return findAll().stream().filter(product -> product.getId().equals(productId)).findFirst().orElse(null);
    }
}
