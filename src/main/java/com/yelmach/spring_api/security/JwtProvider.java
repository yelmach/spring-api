package com.yelmach.spring_api.security;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}") // 24 hours in milliseconds
    private long jwtExpirationMs;

    public String generateToken(User user) {
        try {
            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + jwtExpirationMs);

            return Jwts.builder()
                    .setSubject(user.getEmail())
                    .setIssuedAt(now)
                    .setExpiration(expirationDate)
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token for user {}: {}", user.getEmail(), e.getMessage());
            throw new JwtException("Failed to generate JWT token");
        }
    }

    private Key getSigningKey() {
        try {
            byte[] keyBytes = jwtSecret.getBytes();
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.error("Error creating signing key: {}", e.getMessage());
            throw new JwtException("Failed to create JWT signing key", e);
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token has expired: {}", e.getMessage());
            throw new JwtException("Token has expired.");
        } catch (SignatureException e) {
            logger.warn("JWT token signature validation failed\": {}", e.getMessage());
            throw new JwtException("JWT token signature validation failed");
        } catch (Exception e) {
            logger.warn("invalid JWT token: {}", e.getMessage());
            throw new JwtException("invalid JWT token" + e.getMessage());
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            logger.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            throw new JwtException("JWT token is expired", e);
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            throw new JwtException("JWT token signature validation failed", e);
        } catch (Exception e) {
            logger.error("Unexpected error during JWT validation: {}", e.getMessage());
            throw new JwtException("JWT token validation failed", e);
        }
    }
}