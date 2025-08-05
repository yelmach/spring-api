package com.yelmach.spring_api.dto.response;

import com.yelmach.spring_api.model.Role;

public record UserResponse(
        String id,
        String name,
        String email,
        Role role) {
}