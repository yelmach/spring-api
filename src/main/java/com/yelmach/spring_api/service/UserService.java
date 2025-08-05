package com.yelmach.spring_api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.model.Role;
import com.yelmach.spring_api.model.User;
import com.yelmach.spring_api.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) throws RuntimeException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        return userRepository.save(user);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public User updateUser(String id, User userDetails) throws RuntimeException {
        Optional<User> optionalUser = userRepository.findById(id);

        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found with id: " + id);
        }

        User user = optionalUser.get();

        if (!user.getName().equals(userDetails.getName()) &&
                userRepository.existsByName(userDetails.getName())) {
            throw new RuntimeException("Name already exists");
        }

        if (!user.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        return userRepository.save(user);
    }

    public void deleteUser(String id) throws RuntimeException {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public List<User> searchUsers(String name, String email) throws RuntimeException {
        if ((name.isEmpty()) && (email.isEmpty())) {
            throw new RuntimeException("Please provide either name or email parameter");
        }

        if (!name.isEmpty()) {
            return userRepository.findByNameContainingIgnoreCase(name);
        } else {
            return userRepository.findByEmailContainingIgnoreCase(email);
        }
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
}