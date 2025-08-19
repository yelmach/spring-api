package com.yelmach.spring_api.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yelmach.spring_api.dto.response.ErrorResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errorMessage = "Authentication required to access this resource";
        String errorType = "Unauthorized";

        Object jwtError = request.getAttribute("jwt_error");
        if (jwtError != null) {
            errorMessage = jwtError.toString();
            errorType = "Invalid Token";
        } else if (authException.getMessage() != null) {
            if (authException.getMessage().contains("expired")) {
                errorMessage = "Token has expired. Please login again.";
                errorType = "Token Expired";
            } else if (authException.getMessage().contains("malformed")) {
                errorMessage = "Invalid token format";
                errorType = "Malformed Token";
            } else if (authException.getMessage().contains("signature")) {
                errorMessage = "Token signature verification failed";
                errorType = "Invalid Signature";
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpServletResponse.SC_UNAUTHORIZED,
                errorType,
                errorMessage,
                request.getRequestURI());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}