package com.nikhil.habit_money.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final String userRole;

    public JwtAuthenticationToken(UUID userId, String userRole) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + userRole)));
        this.userId = userId;
        this.userRole = userRole;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public String getUserRole() {
        return userRole;
    }
}
