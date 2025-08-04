package com.yelmach.spring_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yelmach.spring_api.model.Product;
import com.yelmach.spring_api.repository.ProductRepository;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        System.out.println(product);
        return productRepository.save(product);
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String id, @RequestBody Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());

        return productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable String id) {
        productRepository.deleteById(id);
        return "Product with id " + id + " has been deleted";
    }

    @GetMapping("/test")
    public String testDatabase() {
        Product product1 = new Product("Laptop", "Gaming laptop", 999.99);
        Product product2 = new Product("Mouse", "Wireless mouse", 25.50);
        Product product3 = new Product("Keyboard", "Mechanical keyboard", 75.00);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        return "Test data created successfully! Total products: " + productRepository.count();
    }
}