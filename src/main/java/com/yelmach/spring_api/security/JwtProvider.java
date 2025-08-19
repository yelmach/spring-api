package com.yelmach.spring_api.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.yelmach.spring_api.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}") // 24 hours in milliseconds
    private long jwtExpirationMs;

    private Key getSigningKey() {
        try {
            byte[] keyBytes = jwtSecret.getBytes();
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.error("Error creating signing key: {}", e.getMessage());
            throw new JwtException("Failed to create JWT signing key", e);
        }
    }

    public String generateToken(User user) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("role", user.getRole().name());
            claims.put("name", user.getName());

            Date now = new Date();
            Date expireDate = new Date(now.getTime() + jwtExpirationMs);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getEmail())
                    .setIssuedAt(now)
                    .setExpiration(expireDate)
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token for user {}: {}", user.getEmail(), e.getMessage());
            throw new JwtException("Failed to generate JWT token", e);
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
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
            logger.warn("JWT token is expired: {}", e.getMessage());
            throw new JwtException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw new JwtException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
            throw new JwtException("JWT token is malformed", e);
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            throw new JwtException("JWT token signature validation failed", e);
        } catch (IllegalArgumentException e) {
            logger.error("JWT token compact of handler are invalid: {}", e.getMessage());
            throw new JwtException("JWT token is invalid", e);
        } catch (Exception e) {
            logger.error("Unexpected error during JWT parsing: {}", e.getMessage());
            throw new JwtException("JWT token processing failed", e);
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

    public boolean isValidToken(String token, UserDetails userDetails) {
        try {
            final String username = extractEmail(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
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
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw new JwtException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
            throw new JwtException("JWT token is malformed", e);
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            throw new JwtException("JWT token signature validation failed", e);
        } catch (IllegalArgumentException e) {
            logger.error("JWT token is invalid: {}", e.getMessage());
            throw new JwtException("JWT token is invalid", e);
        } catch (Exception e) {
            logger.error("Unexpected error during JWT validation: {}", e.getMessage());
            throw new JwtException("JWT token validation failed", e);
        }
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}