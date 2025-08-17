package com.yelmach.spring_api.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.dto.request.LoginRequest;
import com.yelmach.spring_api.dto.request.UserRegistrationRequest;
import com.yelmach.spring_api.dto.response.AuthResponse;
import com.yelmach.spring_api.dto.response.UserResponse;
import com.yelmach.spring_api.exception.AuthenticationException;
import com.yelmach.spring_api.exception.DuplicateResourceException;
import com.yelmach.spring_api.model.User;
import com.yelmach.spring_api.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.email());

        if (userOptional.isEmpty()) {
            throw new AuthenticationException("Invalid email or password");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        UserResponse userResponse = convertToUserResponse(user);

        return new AuthResponse(token, userResponse);
    }

    public AuthResponse registerUser(UserRegistrationRequest registrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.email())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = new User();
        user.setName(registrationRequest.name());
        user.setEmail(registrationRequest.email());
        user.setPassword(passwordEncoder.encode(registrationRequest.password()));

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        UserResponse userResponse = convertToUserResponse(savedUser);

        return new AuthResponse(token, userResponse);
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole());
    }
}