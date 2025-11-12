package com.yelmach.spring_api.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yelmach.spring_api.model.Role;
import com.yelmach.spring_api.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // MongoRepository provides these methods automatically:
    // - save(User user) - insert or update
    // - findAll() - get all users
    // - findById(String id) - get user by ID
    // - deleteById(String id) - delete by ID
    // - count() - count all users

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByRole(Role role);
}