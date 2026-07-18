package com.nikhil.habit_money.security;

import com.nikhil.habit_money.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser {

    public UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return (UUID) jwtAuth.getPrincipal();
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "No authenticated user");
    }

    public String getRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getUserRole();
        }
        throw new ApplicationException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "No authenticated user");
    }
}
