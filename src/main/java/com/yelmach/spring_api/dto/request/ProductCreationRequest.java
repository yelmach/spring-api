package com.yelmach.spring_api.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductCreationRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    String name,

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 1000, message = "Description must be between 5 and 1000 characters")
    String description,

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @DecimalMax(value = "1000000.00", message = "Price cannot exceed 1_000_000.00")
    Double price
) {}