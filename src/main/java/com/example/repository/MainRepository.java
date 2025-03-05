package com.example.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Primary
@Repository
public abstract class MainRepository<T> {
    public static final Path DATA_DIRECTORY = Path.of("src", "main", "resources", "data");

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected abstract String getDataPath();

    protected abstract Class<T[]> getArrayType();

    public MainRepository() {

    }

    public ArrayList<T> findAll() {
        try {
            File file = new File(getDataPath());
            if (!file.exists()) {
                return new ArrayList<>();
            }
            T[] array = objectMapper.readValue(file, getArrayType()); // Deserialize to array first
            return new ArrayList<>(Arrays.asList(array));
            // return objectMapper.readValue(file, new TypeReference<ArrayList<T>>(){});
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from JSON file", e);
        }
    }

    public void saveAll(ArrayList<T> data) {
        try {
            objectMapper.writeValue(new File(getDataPath()), data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to JSON file", e);
        }
    }

    public void save(T data) {
        ArrayList<T> allData = findAll();
        allData.add(data);
        saveAll(allData);
    }

    public void overrideData(ArrayList<T> data) {
        saveAll(data);
    }

    /**
     * Creates a new entity.
     * 
     * If the entity has an ID, the method checks if an entity with the same ID
     * already exists in the repository. If an entity with the same ID exists, an
     * IllegalArgumentException is thrown.
     * 
     * If the entity does not have an ID, a new UUID is generated and set for the
     * entity before saving it to the repository.
     * 
     * @param model The entity to create
     * @return The created entity
     * @throws IllegalArgumentException if an entity with the same ID already exists
     */
    public T create(T model) {
        if (getIdFromModel(model) != null) {
            var existingModel = findById(getIdFromModel(model));

            if (existingModel != null) {
                throw new IllegalArgumentException("Entity with the same ID already exists");
            }
        } else {
            UUID id = UUID.randomUUID();
            setIdForModel(model, id);
        }

        save(model);
        return model;
    }

    /**
     * Finds an entity by its ID in the repository.
     * 
     * @param id The UUID identifier of the entity to find.
     * @return The entity with the given ID if found, or null if no entity exists
     *         with the given ID.
     */
    public T findById(UUID id) {
        for (T t : findAll()) {
            if (getIdFromModel(t).equals(id)) {
                return t;
            }
        }

        return null;
    }

    /**
     * Updates an entity with the specified ID using the provided update function.
     * 
     * @param id       The UUID of the entity to update
     * @param updateFn A Consumer function that defines the updates to apply to the
     *                 entity
     * @return The updated entity if found, or null if no entity exists with the
     *         given ID
     */
    public T updateById(UUID id, Consumer<T> updateFn) {
        ArrayList<T> allData = findAll();

        for (int i = 0; i < allData.size(); i++) {
            var model = allData.get(i);

            if (getIdFromModel(model).equals(id)) {
                updateFn.accept(model);
                saveAll(allData);
                return model;
            }
        }

        return null;
    }

    /**
     * Updates entities in the repository that satisfy a given predicate.
     * 
     * @param whereFn  A predicate that determines which entities should be updated.
     *                 The predicate should return true for entities to be updated.
     * @param updateFn A consumer that defines the update operation to be performed
     *                 on matched entities. The consumer is applied to each entity
     *                 that satisfies the predicate.
     */
    public void updateWhere(Predicate<T> whereFn, Consumer<T> updateFn) {
        ArrayList<T> allData = findAll();

        for (T model : allData) {
            if (whereFn.test(model)) {
                updateFn.accept(model);
            }
        }

        saveAll(allData);
    }

    /**
     * Deletes an entity by its ID from the repository.
     * 
     * Note: The method returns without any action if no entity with the given ID
     * is found.
     * 
     * @param id The UUID identifier of the entity to delete.
     */
    public void deleteById(UUID id) {
        ArrayList<T> allData = findAll();

        for (int i = 0; i < allData.size(); i++) {
            if (getIdFromModel(allData.get(i)).equals(id)) {
                allData.remove(i);
                saveAll(allData);
                return;
            }
        }
    }

    /**
     * Gets the UUID identifier from a model object.
     * This method must be overridden in repository subclasses to provide the
     * correct ID extraction logic.
     * 
     * Generic methods in MainRepository such as deleteById() and findById() rely on
     * this method
     * to extract the ID from model objects for database operations.
     *
     * @param model The model object to get the ID from
     * @return The UUID identifier of the model
     * @throws UnsupportedOperationException if the method is not overridden in the
     *                                       repository subclass
     * 
     * @see MainRepository#deleteById
     * @see MainRepository#findById
     */
    public UUID getIdFromModel(T model) {
        throw new UnsupportedOperationException(
                "getIdFromModel() must be implemented in repository subclass to " +
                        "extract and return the UUID identifier from your model object.\n" +
                        "Example implementation:\n" +
                        "    @Override\n" +
                        "    public UUID getIdFromModel(Model model) {\n" +
                        "        return model.getId();  // Assuming your model has getId() method\n" +
                        "    }\n" +
                        "This is required for generic methods in MainRepository<T> such as findById() and deleteById() to work.");
    }

    /**
     * Sets the UUID identifier for a model object.
     * This method must be overridden in repository subclasses to provide the
     * correct ID setting logic.
     * 
     * Generic methods in MainRepository such as create() rely on this method
     * to set the ID for model objects before saving them to the database.
     *
     * @param model The model object to set the ID for
     * @param id    The UUID identifier to set for the model
     * @throws UnsupportedOperationException if the method is not overridden in the
     *                                       repository subclass
     * 
     * @see MainRepository#create
     */
    public void setIdForModel(T model, UUID id) {
        throw new UnsupportedOperationException(
                "setIdForModel() must be implemented in repository subclass to " +
                        "set the UUID identifier for your model object.\n" +
                        "Example implementation:\n" +
                        "    @Override\n" +
                        "    public void setIdForModel(Model model, UUID id) {\n" +
                        "        model.setId(id);  // Assuming your model has setId() method\n" +
                        "    }\n" +
                        "This is required for generic methods in MainRepository<T> such as create() to work.");
    }
}
