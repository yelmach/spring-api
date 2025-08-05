package com.yelmach.spring_api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Product createProduct(Product product) throws RuntimeException {
        if (!product.getUserId().isEmpty()) {
            if (!userRepository.existsById(product.getUserId())) {
                throw new RuntimeException("User not found with id: " + product.getUserId());
            }

            if (productRepository.existsByNameAndUserId(product.getName(), product.getUserId())) {
                throw new RuntimeException("Product with this name already exists for this user");
            }
        }
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public Product updateProduct(String id, Product productDetails) throws RuntimeException {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (!optionalProduct.isPresent()) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        Product product = optionalProduct.get();

        if (!productDetails.getUserId().isEmpty() && !userRepository.existsById(productDetails.getUserId())) {
            throw new RuntimeException("User not found with id: " + productDetails.getUserId());
        }

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());

        return productRepository.save(product);
    }

    public void deleteProduct(String id) throws RuntimeException {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public List<Product> getProductsByUserId(String userId) throws RuntimeException {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return productRepository.findByUserId(userId);
    }

    public List<Product> searchProducts(String name) throws RuntimeException {
        if (name.trim().isEmpty()) {
            throw new RuntimeException("Search query cannot be empty");
        }
        return productRepository.findByName(name.trim());
    }

    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) throws RuntimeException {
        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            throw new RuntimeException("Invalid price range. Min price must be >= 0 and <= max price");
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