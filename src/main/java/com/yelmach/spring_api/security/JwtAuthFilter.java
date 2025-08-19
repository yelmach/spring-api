package com.yelmach.spring_api.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtProvider jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = getJwt(request);

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String userEmail = jwtService.extractEmail(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                    if (jwtService.isValidToken(jwt)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        request.setAttribute("jwt_error", "Token validation failed");
                    }
                } catch (UsernameNotFoundException e) {
                    logger.error("User not found: {}", userEmail);
                    request.setAttribute("jwt_error", "User not found");
                }
            }
        } catch (JwtException e) {
            logger.error("JWT processing error: {}", e.getMessage());
            String errorMessage = "Invalid token";

            if (e.getMessage().contains("expired")) {
                errorMessage = "Token has expired";
            } else if (e.getMessage().contains("malformed")) {
                errorMessage = "Token format is invalid";
            } else if (e.getMessage().contains("signature")) {
                errorMessage = "Token signature is invalid";
            } else if (e.getMessage().contains("unsupported")) {
                errorMessage = "Token type is not supported";
            }

            request.setAttribute("jwt_error", errorMessage);
        } catch (Exception e) {
            logger.error("Unexpected error during JWT processing: {}", e.getMessage());
            request.setAttribute("jwt_error", "Authentication failed");
        }

        filterChain.doFilter(request, response);
    }

    private String getJwt(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}