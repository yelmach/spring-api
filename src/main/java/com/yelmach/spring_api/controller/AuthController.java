package com.yelmach.spring_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yelmach.spring_api.dto.request.LoginRequest;
import com.yelmach.spring_api.dto.request.UserRegistrationRequest;
import com.yelmach.spring_api.dto.response.AuthResponse;
import com.yelmach.spring_api.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        AuthResponse authResponse = authService.registerUser(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }
}