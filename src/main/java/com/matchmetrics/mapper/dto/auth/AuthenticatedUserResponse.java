package com.matchmetrics.mapper.dto.auth;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUserResponse {

    private Long userId;
    private String email;
    private String fullName;
    private UserRole role;
    private UserStatus status;
}