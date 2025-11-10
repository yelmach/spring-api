package com.yelmach.spring_api.dto.response;

import com.yelmach.spring_api.model.Role;
import com.yelmach.spring_api.model.User;

public record UserResponse(
                String id,
                String name,
                String email,
                Role role) {
        public static UserResponse fromUser(User user) {
                return new UserResponse(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getRole());
        }
}