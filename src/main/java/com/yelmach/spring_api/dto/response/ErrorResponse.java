package com.yelmach.spring_api.dto.response;

public record ErrorResponse(
        int status,

        String error,

        String message,

        Object details) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, null);
    }
}