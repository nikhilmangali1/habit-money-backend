package com.nikhil.habit_money.auth.dto.response;

import com.nikhil.habit_money.users.enums.AuthProvider;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {

    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private AuthProvider authProvider;
    private String tokenType;
}
