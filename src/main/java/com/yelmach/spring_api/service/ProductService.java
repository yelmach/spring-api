package com.yelmach.spring_api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.dto.request.ProductCreationRequest;
import com.yelmach.spring_api.dto.request.ProductUpdateRequest;
import com.yelmach.spring_api.exception.InvalidRequestException;
import com.yelmach.spring_api.exception.ResourceNotFoundException;
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
        if (userId != null && !userId.isEmpty()) {
            if (!userRepository.existsById(userId)) {
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }
        }

        Product product = new Product(request.name(), request.description(), request.price(), userId);
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public Product updateProductByIdAndUserId(String id, ProductUpdateRequest request, String userId) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (!optionalProduct.isPresent()) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        Product product = optionalProduct.get();

        if (product.getUserId() != null && !product.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("you don't have permission to update it");
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

    public void deleteProductByIdAndUserId(String id, String userId) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (!optionalProduct.isPresent()) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        Product product = optionalProduct.get();

        if (product.getUserId() != null && !product.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("you don't have permission to delete it");
        }

        productRepository.deleteById(id);
    }

    public List<Product> getProductsByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return productRepository.findByUserId(userId);
    }

    public List<Product> searchProducts(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException("Name parameter is required");
        }
        return productRepository.findByNameContainingIgnoreCase(name.trim());
    }

    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            throw new InvalidRequestException("Invalid price range. Min price must be >= 0 and <= max price");
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Product> getHighestPriceProducts() {
        return productRepository.findTop10ByOrderByPriceDesc();
    }

    public List<Product> getLowestPriceProducts() {
        return productRepository.findTop10ByOrderByPriceAsc();
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

    public void createTestProducts() {
        Product product1 = new Product("Laptop", "Gaming laptop", 999.99);
        Product product2 = new Product("Mouse", "Wireless mouse", 25.50);
        Product product3 = new Product("Keyboard", "Mechanical keyboard", 75.00);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
    }

    public long getProductCount() {
        return productRepository.count();
    }
}