package com.yelmach.spring_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yelmach.spring_api.model.Product;
import com.yelmach.spring_api.repository.ProductRepository;
import com.yelmach.spring_api.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching products: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        try {
            if (!product.getUserId().isEmpty()) {
                if (!userRepository.existsById(product.getUserId())) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("User not found with id: " + product.getUserId()));
                }

                if (productRepository.existsByNameAndUserId(product.getName(), product.getUserId())) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Product with this name already exists for this user"));
                }
            }

            Product savedProduct = productRepository.save(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating product: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        try {
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                return ResponseEntity.ok(product.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Product not found with id: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching product: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody Product productDetails) {
        try {
            Optional<Product> optionalProduct = productRepository.findById(id);

            if (!optionalProduct.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Product not found with id: " + id));
            }

            Product product = optionalProduct.get();

            if (!productDetails.getUserId().isEmpty() && !userRepository.existsById(productDetails.getUserId())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("User not found with id: " + productDetails.getUserId()));
            }

            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());

            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(updatedProduct);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        try {
            if (!productRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Product not found with id: " + id));
            }

            productRepository.deleteById(id);
            return ResponseEntity.ok(createSuccessResponse("Product with id " + id + " has been deleted"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deleting product: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getProductsByUserId(@PathVariable String userId) {
        try {
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("User not found with id: " + userId));
            }

            List<Product> products = productRepository.findByUserId(userId);
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching products for user: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam String name) {
        try {
            if (name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Search query cannot be empty"));
            }

            List<Product> products = productRepository.findByName(name.trim());
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error searching products: " + e.getMessage()));
        }
    }

    @GetMapping("/price-range")
    public ResponseEntity<?> getProductsByPriceRange(@RequestParam double minPrice, @RequestParam double maxPrice) {
        try {
            if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid price range. Min price must be >= 0 and <= max price"));
            }

            List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching products by price range: " + e.getMessage()));
        }
    }

    @GetMapping("/highest-price")
    public ResponseEntity<?> getHighestPriceProducts() {
        try {
            List<Product> products = productRepository.findTop10ByOrderByPriceDesc();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching most expensive products: " + e.getMessage()));
        }
    }

    @GetMapping("/lowest-price")
    public ResponseEntity<?> getLowestPriceProducts() {
        try {
            List<Product> products = productRepository.findTop10ByOrderByPriceAsc();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching least expensive products: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getProductStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", productRepository.count());

            // Get user-specific stats if needed
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

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching product statistics: " + e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<?> createTestProducts() {
        try {
            // Create products without user association for testing
            Product product1 = new Product("Laptop", "Gaming laptop", 999.99);
            Product product2 = new Product("Mouse", "Wireless mouse", 25.50);
            Product product3 = new Product("Keyboard", "Mechanical keyboard", 75.00);

            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);

            return ResponseEntity.ok(createSuccessResponse(
                    "Test products created successfully! Total products: " + productRepository.count()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating test products: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}