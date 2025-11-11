package com.yelmach.spring_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yelmach.spring_api.dto.request.UpdateUserRequest;
import com.yelmach.spring_api.dto.response.UserResponse;
import com.yelmach.spring_api.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.getCurrentUser();

        UserResponse updatedUser = userService.updateUser(user.id(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping()
    public ResponseEntity<Map<String, Object>> deleteUser() {
        UserResponse user = userService.getCurrentUser();

        UserResponse deletedUser = userService.deleteUser(user.id());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "User has been deleted");
        response.put("userDetails", deletedUser);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = userService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String id) {
        UserResponse deletedUser = userService.deleteUser(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "User has been deleted");
        response.put("userDetails", deletedUser);

        return ResponseEntity.ok(response);
    }
}