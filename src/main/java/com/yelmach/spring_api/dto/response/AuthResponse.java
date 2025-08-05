package com.yelmach.spring_api.dto.response;

public record AuthResponse(
        String token,
        String type,
        UserResponse user) {
    public AuthResponse(String token, UserResponse user) {
        this(token, "Bearer", user);
    }
}