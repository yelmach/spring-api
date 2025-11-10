package com.yelmach.spring_api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.dto.request.UpdateUserRequest;
import com.yelmach.spring_api.dto.response.UserResponse;
import com.yelmach.spring_api.exception.ApiException;
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
                .map(UserResponse::fromUser)
                .toList();
    }

    public Optional<UserResponse> getUserById(@NonNull String id) {
        return userRepository.findById(id)
                .map(UserResponse::fromUser);
    }

    public UserResponse updateUser(@NonNull String id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(
                () -> ApiException.notFound("User not found with id: " + id));

        if (request.email() != null && !user.getEmail().equals(request.email()) &&
                userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("Email already exists");
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
        return UserResponse.fromUser(updatedUser);
    }

    public UserResponse deleteUser(String id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> ApiException.notFound("User not found with id: " + id));

        userRepository.deleteById(id);
        return UserResponse.fromUser(user);
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("adminCount", userRepository.countByRole(Role.ADMIN));
        stats.put("userCount", userRepository.countByRole(Role.USER));
        return stats;
    }
}