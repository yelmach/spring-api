package com.yelmach.spring_api.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoException;
import com.yelmach.spring_api.dto.response.ErrorResponse;

import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Resource Not Found",
                                ex.getMessage(),
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "Duplicate Resource",
                                ex.getMessage(),
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(InvalidRequestException.class)
        public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Request",
                                ex.getMessage(),
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(com.yelmach.spring_api.exception.AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleCustomAuthenticationException(
                        com.yelmach.spring_api.exception.AuthenticationException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Authentication Failed",
                                ex.getMessage(),
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleSpringAuthenticationException(AuthenticationException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Authentication Failed",
                                "Invalid credentials or authentication required",
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Invalid Credentials",
                                "The provided credentials are invalid",
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "Access Denied",
                                "You don't have permission to access this resource",
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(JwtException.class)
        public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex, WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "JWT Error",
                                "Invalid or expired token: " + ex.getMessage(),
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                        WebRequest request) {

                List<String> details = new ArrayList<>();
                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        details.add(error.getField() + ": " + error.getDefaultMessage());
                }

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Failed",
                                "Request validation failed",
                                details);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Argument",
                                ex.getMessage(),
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex,
                        WebRequest request) {

                String message = "Invalid JSON format or malformed request body";
                if (ex.getCause() instanceof JsonProcessingException) {
                        message = "JSON parsing error: " + ex.getCause().getMessage();
                }

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Malformed Request",
                                message,
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException ex,
                        WebRequest request) {

                String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Parameter Type",
                                message,
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
                        MissingServletRequestParameterException ex,
                        WebRequest request) {

                String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Missing Parameter",
                                message,
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException ex,
                        WebRequest request) {

                String message = String.format(
                                "HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                                ex.getMethod(), String.join(", ", ex.getSupportedMethods()));

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.METHOD_NOT_ALLOWED.value(),
                                "Method Not Allowed",
                                message,
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
                        HttpMediaTypeNotSupportedException ex,
                        WebRequest request) {

                String message = String.format("Content type '%s' is not supported. Supported types: %s",
                                ex.getContentType(), ex.getSupportedMediaTypes());

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                                "Unsupported Media Type",
                                message,
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        // Database exceptions
        @ExceptionHandler(DuplicateKeyException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException ex,
                        WebRequest request) {

                String message = "Duplicate entry detected. This resource already exists.";
                if (ex.getMessage().contains("email")) {
                        message = "Email address is already registered";
                } else if (ex.getMessage().contains("name")) {
                        message = "This name is already taken";
                }

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "Duplicate Entry",
                                message,
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(MongoException.class)
        public ResponseEntity<ErrorResponse> handleMongoException(MongoException ex, WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                "Database Error",
                                "Database service is temporarily unavailable. Please try again later.",
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
                ex.printStackTrace();

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred. Please try again later.",
                                getPath(request));

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private String getPath(WebRequest request) {
                String description = request.getDescription(false);
                if (description.startsWith("uri=")) {
                        return description.substring(4);
                }
                return description;
        }
}