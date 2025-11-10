package com.yelmach.spring_api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.dto.request.UpdateUserRequest;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        return UserResponse.fromUser(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .toList();
    }

    public Optional<UserResponse> getUserById(String id) {
        return userRepository.findById(id)
                .map(this::convertToUserResponse);
    }

    public UserResponse updateUser(String id, UpdateUserRequest request) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        User user = optionalUser.get();

        if (request.email() != null && !user.getEmail().equals(request.email()) &&
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
            user.setPassword(passwordEncoder.encode(request.password()));
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

    public List<UserResponse> searchUsersByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidRequestException("Email parameter is required");
        }

        List<User> users = userRepository.findByEmailContainingIgnoreCase(email.trim());
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

    public long getUserCount() {
        return userRepository.count();
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}