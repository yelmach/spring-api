package com.yelmach.spring_api.dto.response;

import java.util.List;

public record ErrorResponse(
        int status,

        String error,

        String message,

        String path,

        List<String> details) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, null);
    }
}