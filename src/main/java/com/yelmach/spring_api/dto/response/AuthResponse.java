package com.yelmach.spring_api.dto.response;

public record AuthResponse(String token, UserResponse user) {
}