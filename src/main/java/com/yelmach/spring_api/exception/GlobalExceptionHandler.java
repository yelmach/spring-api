package com.yelmach.spring_api.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.mongodb.MongoException;
import com.yelmach.spring_api.dto.response.ErrorResponse;

import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(ApiException.class)
        public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                ex.getStatus(),
                                ex.getError(),
                                ex.getMessage(),
                                ex.getData());

                return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleSpringAuthenticationException(AuthenticationException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Authentication Failed",
                                "Invalid credentials or authentication required");

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Invalid credentials");

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                404,
                                "Not Found",
                                "User not found");

                return ResponseEntity.status(404).body(errorResponse);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "Access Denied",
                                "You don't have permission to access this resource");

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(JwtException.class)
        public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex, WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "JWT Error",
                                ex.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                        WebRequest request) {
                Map<String, String> fieldErrors = new HashMap<>();

                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        fieldErrors.put(error.getField(), error.getDefaultMessage());
                }

                ErrorResponse errorResponse = new ErrorResponse(
                                400,
                                "Validation Failed",
                                "Please check your input data",
                                fieldErrors);

                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Argument",
                                ex.getMessage());

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
                                message);

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
                                message);

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
                                message);

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
                                message);

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
                                message);

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(MongoException.class)
        public ResponseEntity<ErrorResponse> handleMongoException(MongoException ex, WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                "Database Error",
                                "Database service is temporarily unavailable. Please try again later.");

                return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
                ex.printStackTrace();

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred. Please try again later.");

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
