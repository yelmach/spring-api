package com.yelmach.spring_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.dto.request.LoginRequest;
import com.yelmach.spring_api.dto.request.RegisterRequest;
import com.yelmach.spring_api.dto.response.AuthResponse;
import com.yelmach.spring_api.dto.response.UserResponse;
import com.yelmach.spring_api.exception.ApiException;
import com.yelmach.spring_api.model.Role;
import com.yelmach.spring_api.model.User;
import com.yelmach.spring_api.repository.UserRepository;
import com.yelmach.spring_api.security.JwtProvider;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtService;

    @Autowired
    private UserRepository userRepository;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, UserResponse.fromUser(user));
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.badRequest("Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(token, UserResponse.fromUser(savedUser));
    }
}