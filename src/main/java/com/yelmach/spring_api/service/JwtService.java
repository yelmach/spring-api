package com.yelmach.spring_api.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}") // 24 hours in milliseconds
    private long jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("name", user.getName());

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder().setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey())
                .compact();
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
            throw new JwtException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("JWT token is malformed", e);
        } catch (SecurityException | IllegalArgumentException e) {
            throw new JwtException("JWT token signature validation failed", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        try {
            final String username = extractEmail(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("JWT token is malformed", e);
        } catch (SecurityException | IllegalArgumentException e) {
            throw new JwtException("JWT token signature validation failed", e);
        }
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
