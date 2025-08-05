package com.yelmach.spring_api.dto.request;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters") String name,

        @Size(max = 100, message = "Email must not axceed 100 characters long") String email,

        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password) {
}
