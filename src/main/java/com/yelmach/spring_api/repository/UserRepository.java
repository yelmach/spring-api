package com.yelmach.spring_api.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.yelmach.spring_api.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    @Query("{'$or': [{'username': ?0}, {'email': ?0}]}")
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
}
