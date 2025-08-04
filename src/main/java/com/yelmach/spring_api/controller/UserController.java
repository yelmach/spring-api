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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yelmach.spring_api.model.Role;
import com.yelmach.spring_api.model.User;
import com.yelmach.spring_api.repository.UserRepository;

import jakarta.validation.Valid;

@RestController("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            if (userRepository.existsByEmail(user.getEmail())) {
                String msg = "Email already exists";
                return ResponseEntity.badRequest().body(createErrorResponse(msg));
            }

            User savedUser = userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating user: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found with id: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody User userDetails) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);

            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found with id: " + id));
            }

            User user = optionalUser.get();

            if (!user.getName().equals(userDetails.getName()) &&
                    userRepository.existsByName(userDetails.getName())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Username already exists"));
            }

            if (!user.getEmail().equals(userDetails.getEmail()) &&
                    userRepository.existsByEmail(userDetails.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Email already exists"));
            }

            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());

            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(userDetails.getPassword());
            }

            if (userDetails.getRole() != null) {
                user.setRole(userDetails.getRole());
            }

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            if (!userRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found with id: " + id));
            }

            userRepository.deleteById(id);
            return ResponseEntity.ok(createSuccessResponse("User with id " + id + " has been deleted"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deleting user: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {
        try {
            List<User> users;

            if (!name.isEmpty() && !email.isEmpty()) {
                users = userRepository.findByNameContainingIgnoreCase(name);
                users = userRepository.findByEmailContainingIgnoreCase(email);
            } else if (!name.isEmpty()) {
                users = userRepository.findByNameContainingIgnoreCase(name);
            } else if (!email.isEmpty()) {
                users = userRepository.findByEmailContainingIgnoreCase(email);
            } else {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Please provide either name or email parameter"));
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error searching users: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userRepository.count());
            stats.put("adminCount", userRepository.countByRole(Role.ADMIN));
            stats.put("userCount", userRepository.countByRole(Role.USER));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching user statistics: " + e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<?> createTestUsers() {
        try {
            // Create admin user
            User admin = new User("admin", "admin@admin.com", "admin123", Role.ADMIN);
            if (!userRepository.existsByEmail("admin@admin.com")) {
                userRepository.save(admin);
            }

            // Create regular users
            User user1 = new User("user1", "user1@user.com", "user123");
            User user2 = new User("user2", "user2@user.com", "user123");

            if (!userRepository.existsByEmail("user1@user.com")) {
                userRepository.save(user1);
            }
            if (!userRepository.existsByEmail("user2@user.com")) {
                userRepository.save(user2);
            }

            return ResponseEntity.ok(createSuccessResponse(
                    "Test users created successfully! Total users: " + userRepository.count()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating test users: " + e.getMessage()));
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
