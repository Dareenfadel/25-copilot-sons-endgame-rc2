package com.example.repository;

import com.example.model.User;
import org.springframework.stereotype.Repository;


import java.util.UUID;
@Repository
public class UserRepository extends MainRepository<User> {

    public UserRepository() {
    }
    @Override
    protected String getDataPath() {
        return "src\\main\\java\\com\\example\\data\\users.json";
    }

    @Override
    protected Class<User[]> getArrayType() {
        return User[].class;
    }
    public User getUserById(UUID userId){


        return findAll().stream().filter(user -> user.getId().equals(userId)).findFirst().orElse(null);
    }


}
