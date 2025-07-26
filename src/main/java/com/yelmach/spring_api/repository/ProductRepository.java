package com.yelmach.spring_api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.yelmach.spring_api.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {

}
