package com.example.service;

import com.example.repository.MainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

@Primary
@Service
public abstract class MainService<T> {

    protected final MainRepository<T> repository; // Dependency Injection for repository
    private final String entityType; // The entity type of the service
    
    @Autowired
    public MainService(MainRepository<T> repository, String entityType) {
        this.repository = repository;
        this.entityType = entityType;
    }

    // Validate if an entity exists, including class name in the error message
    protected void validateExistence(UUID id) {
        if (repository.findById(id) == null) {
            throw new NoSuchElementException(entityType + " not found");
        }
    }
    
    // ----------------------
    // CRUD Operations
    // ----------------------
    
    public T create(T entity) {
        return repository.create(entity);
    }

    public List<T> findAll() {
        return repository.findAll();
    }
    
    public T findById(UUID id) {
        return repository.findById(id);
    }
    
    public T updateById(UUID id, Consumer<T> consumer) {
        return repository.updateById(id, consumer);
    }
    
    public void updateEach(Consumer<T> consumer) {
        repository.updateEach(consumer);
    }
    
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
    
    public void replaceModelsInList(List<T> list, T newModel) {
        var newModelId = repository.getIdFromModel(newModel);
        
        for (int i = 0; i < list.size(); i++) {
            var model = list.get(i);
            var modelId = repository.getIdFromModel(model);
            if (modelId.equals(newModelId)) {
                list.set(i, newModel);
            }
        }
    }
    
    public void deleteModelFromListById(List<T> list, UUID id) {
        for (int i = 0; i < list.size(); i++) {
            var model = list.get(i);
            var modelId = repository.getIdFromModel(model);
            if (modelId.equals(id)) {
                list.remove(i);
            }
        }
    }
}
