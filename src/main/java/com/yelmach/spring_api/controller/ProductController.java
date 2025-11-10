package com.yelmach.spring_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yelmach.spring_api.dto.request.ProductCreationRequest;
import com.yelmach.spring_api.dto.request.ProductUpdateRequest;
import com.yelmach.spring_api.model.Product;
import com.yelmach.spring_api.model.User;
import com.yelmach.spring_api.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(@RequestParam double min, @RequestParam double max) {
        List<Product> products = productService.getProductsByPriceRange(min, max);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/highest-price")
    public ResponseEntity<List<Product>> getHighestPriceProducts() {
        List<Product> products = productService.getHighestPriceProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/lowest-price")
    public ResponseEntity<List<Product>> getLowestPriceProducts() {
        List<Product> products = productService.getLowestPriceProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchProducts(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/me")
    public ResponseEntity<List<Product>> getMyProducts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String userId = currentUser.getId();

        List<Product> products = productService.getProductsByUserId(userId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        Map<String, Object> stats = productService.getProductStats();
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String userId = currentUser.getId();

        productService.deleteProductByIdAndUserId(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product with id " + id + " has been deleted");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id,
            @Valid @RequestBody ProductUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String userId = currentUser.getId();

        Product updatedProduct = productService.updateProductByIdAndUserId(id, request, userId);
        return ResponseEntity.ok(updatedProduct);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String userId = currentUser.getId();

        Product savedProduct = productService.createProduct(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
}