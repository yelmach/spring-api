package com.yelmach.spring_api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.dto.request.UserRegistrationRequest;
import com.yelmach.spring_api.dto.request.UserUpdateRequest;
import com.yelmach.spring_api.dto.response.UserResponse;
import com.yelmach.spring_api.exception.DuplicateResourceException;
import com.yelmach.spring_api.exception.InvalidRequestException;
import com.yelmach.spring_api.exception.ResourceNotFoundException;
import com.yelmach.spring_api.model.Role;
import com.yelmach.spring_api.model.User;
import com.yelmach.spring_api.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .toList();
    }

    public UserResponse createUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User(request.name(), request.email(), request.password());
        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    public Optional<UserResponse> getUserById(String id) {
        return userRepository.findById(id)
                .map(this::convertToUserResponse);
    }

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        User user = optionalUser.get();

        if (!user.getName().equals(request.name()) &&
                userRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (!user.getEmail().equals(request.email()) &&
                userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(request.password());
        }

        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public List<UserResponse> searchUsers(String name, String email) {
        if (name.isEmpty() && email.isEmpty()) {
            throw new InvalidRequestException("Please provide either name or email parameter");
        }

        List<User> users;
        if (!name.isEmpty()) {
            users = userRepository.findByNameContainingIgnoreCase(name);
        } else {
            users = userRepository.findByEmailContainingIgnoreCase(email);
        }

        return users.stream()
                .map(this::convertToUserResponse)
                .toList();
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("adminCount", userRepository.countByRole(Role.ADMIN));
        stats.put("userCount", userRepository.countByRole(Role.USER));
        return stats;
    }

    public void createTestUsers() {
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
    }

    public long getUserCount() {
        return userRepository.count();
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}