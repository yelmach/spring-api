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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errorMessage = "Authentication required to access this resource";

        Object jwtError = request.getAttribute("jwt_error");
        if (jwtError != null) {
            errorMessage = jwtError.toString();
        }

        ErrorResponse err = new ErrorResponse(401, "Unauthorized", errorMessage);

        String jsonResponse = objectMapper.writeValueAsString(err);
        response.getWriter().write(jsonResponse);
    }
}
