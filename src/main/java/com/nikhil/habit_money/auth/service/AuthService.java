package com.nikhil.habit_money.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.nikhil.habit_money.auth.dto.request.GoogleLoginRequest;
import com.nikhil.habit_money.auth.dto.request.LoginRequest;
import com.nikhil.habit_money.auth.dto.request.LogoutRequest;
import com.nikhil.habit_money.auth.dto.request.RefreshTokenRequest;
import com.nikhil.habit_money.auth.dto.request.RegisterRequest;
import com.nikhil.habit_money.auth.dto.response.AuthResponse;
import com.nikhil.habit_money.auth.dto.response.RegisterResponse;
import com.nikhil.habit_money.auth.entity.RefreshTokenEntity;
import com.nikhil.habit_money.auth.repository.JpaRefreshTokenRepository;
import com.nikhil.habit_money.common.exceptions.ResourceNotFoundException;
import com.nikhil.habit_money.common.exceptions.ValidationException;
import com.nikhil.habit_money.users.entity.UserEntity;
import com.nikhil.habit_money.users.enums.AuthProvider;
import com.nikhil.habit_money.users.enums.UserRole;
import com.nikhil.habit_money.users.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JpaUserRepository userRepository;
    private final JpaRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .authProvider(AuthProvider.EMAIL)
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        return RegisterResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new ValidationException("This account uses Google Sign-In. Please sign in with Google.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ValidationException("Invalid email or password");
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.getGoogleToken());
        if (payload == null) {
            throw new ValidationException("Invalid Google token");
        }

        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");

        if (firstName == null) firstName = (String) payload.get("name");
        if (lastName == null) lastName = "";

        UserEntity user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = UserEntity.builder()
                    .email(email)
                    .passwordHash(null)
                    .firstName(firstName)
                    .lastName(lastName)
                    .authProvider(AuthProvider.GOOGLE)
                    .role(UserRole.USER)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenString = request.getRefreshToken();

        if (!jwtService.isTokenValid(tokenString)) {
            throw new ValidationException("Invalid refresh token");
        }

        var claims = jwtService.extractClaims(tokenString);
        if (!"REFRESH".equals(claims.get("tokenType", String.class))) {
            throw new ValidationException("Invalid token type");
        }

        RefreshTokenEntity storedToken = refreshTokenRepository
                .findByRefreshToken(tokenString)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (storedToken.isRevoked()) {
            throw new ValidationException("Refresh token revoked");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Refresh token expired");
        }

        UserEntity user = storedToken.getUser();

        refreshTokenRepository.delete(storedToken);

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        RefreshTokenEntity storedToken = refreshTokenRepository
                .findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        refreshTokenRepository.delete(storedToken);
    }

    private AuthResponse generateAuthResponse(UserEntity user) {
        String userId = user.getId().toString();
        String role = user.getRole().name();

        String accessToken = jwtService.generateAccessToken(userId, role);
        String refreshToken = jwtService.generateRefreshToken(userId, role);

        refreshTokenRepository.deleteAllByUser(user);

        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(tokenEntity);

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(role)
                .authProvider(user.getAuthProvider())
                .tokenType("Bearer")
                .build();
    }
}
