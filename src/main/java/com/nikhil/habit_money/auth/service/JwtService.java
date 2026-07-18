package com.nikhil.habit_money.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenValidity,
            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenValidity) {
        this.key = Keys.hmacShaKeyFor(Arrays.copyOf(secret.getBytes(), 64));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public String generateAccessToken(String userId, String role) {
        return buildToken(userId, role, "ACCESS", accessTokenValidity);
    }

    public String generateRefreshToken(String userId, String role) {
        return buildToken(userId, role, "REFRESH", refreshTokenValidity);
    }

    private String buildToken(String userId, String role, String tokenType, long validity) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .claim("tokenType", tokenType)
                .issuedAt(new Date(now))
                .expiration(new Date(now + validity))
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
}
