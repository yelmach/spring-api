package com.yelmach.spring_api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.dto.request.ProductCreationRequest;
import com.yelmach.spring_api.dto.request.ProductUpdateRequest;
import com.yelmach.spring_api.exception.ApiException;
import com.yelmach.spring_api.model.Product;
import com.yelmach.spring_api.repository.ProductRepository;
import com.yelmach.spring_api.repository.UserRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(ProductCreationRequest request, String userId) {
        if (!userRepository.existsById(userId)) {
            throw ApiException.notFound("User not found with id: " + userId);
        }

        Product product = new Product(request.name(), request.description(), request.price(), userId);
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public Product updateProduct(String id, ProductUpdateRequest request, String userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("producy not found with id: " + userId));

        if (!product.getUserId().equals(userId)) {
            throw ApiException.badRequest("you are not the owner of this product");
        }

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }

        return productRepository.save(product);
    }

    public void deleteProduct(String id, String userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("producy not found with id: " + userId));

        if (!product.getUserId().equals(userId)) {
            throw ApiException.badRequest("you are not the owner of this product");
        }

        productRepository.deleteById(id);
    }

    public List<Product> getProductsByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw ApiException.notFound("User not found with id: " + userId);
        }

        return productRepository.findByUserId(userId);
    }

    public Map<String, Object> getProductStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productRepository.count());

        List<Object[]> userProductCounts = productRepository.findAll()
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getUserId() != null ? p.getUserId() : "No User",
                        java.util.stream.Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new Object[] { entry.getKey(), entry.getValue() })
                .collect(java.util.stream.Collectors.toList());

        stats.put("productsByUser", userProductCounts);
        return stats;
    }
}