package com.matchmetrics.mapper.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    @JsonIgnore
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private UserRole role;
    private UserStatus status;

    public AuthResponse withoutToken() {
        return new AuthResponse(null, userId, email, fullName, role, status);
    }
}