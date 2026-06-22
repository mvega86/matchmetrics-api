package com.matchmetrics.mapper.dto.auth;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;

public record AuthMeResponse(
        Long id,
        String email,
        String fullName,
        UserRole role,
        UserStatus status,
        Long teamId,
        String teamName,
        String requestedTeamName,
        String avatarUrl,
        String phone,
        String bio
) {
}