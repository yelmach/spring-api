package com.yelmach.spring_api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.yelmach.spring_api.model.Product;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // MongoRepository provides these methods automatically:
    // - save(Product product) - insert or update
    // - findAll() - get all products
    // - findById(String id) - get product by ID
    // - deleteById(String id) - delete by ID
    // - count() - count all products

    List<Product> findByName(String name);

    List<Product> findByPriceGreaterThan(double price);

    List<Product> findByNameContainingIgnoreCase(String name);
}