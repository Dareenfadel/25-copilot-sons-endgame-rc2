package com.example.service;


import com.example.repository.MainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Primary
@Service
public abstract class MainService<T> {

    protected final MainRepository<T> repository; // Dependency Injection for repository
        private final String entityType; // The entity type of the service

    @Autowired
    protected MainService(MainRepository<T> repository,  String entityType) {
        this.repository = repository;
        this.entityType = entityType;
    }

    // Validate if an entity exists, including class name in the error message
    protected void validateExistence(UUID id) {
        if (repository.findById(id) == null) {
            throw new NoSuchElementException(entityType+ " not found");
        }
    }
}
