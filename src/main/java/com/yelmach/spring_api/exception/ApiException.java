package com.yelmach.spring_api.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final int status;
    private final String error;
    private final Object data;

    public ApiException(int status, String error, String message, Object data) {
        super(message);
        this.status = status;
        this.error = error;
        this.data = data;
    }

    public ApiException(int status, String error, String message) {
        this(status, error, message, null);
    }

    public ApiException(HttpStatus httpStatus, String message) {
        this(httpStatus.value(), httpStatus.getReasonPhrase(), message, null);
    }

    public ApiException(HttpStatus httpStatus, String message, Object data) {
        this(httpStatus.value(), httpStatus.getReasonPhrase(), message, data);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException badRequest(String message, Object data) {
        return new ApiException(HttpStatus.BAD_REQUEST, message, data);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException conflict(String message, Object data) {
        return new ApiException(HttpStatus.CONFLICT, message, data);
    }

    public static ApiException internalError(String message) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public Object getData() {
        return data;
    }
}