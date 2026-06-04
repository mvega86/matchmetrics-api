package com.matchmetrics.mapper.dto.admin;

import com.matchmetrics.domain.enums.AuthProvider;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingUserResponse {

    private Long userId;
    private String email;
    private String fullName;
    private AuthProvider provider;
    private UserRole role;
    private UserStatus status;
    private Long teamId;
    private String teamName;
    private String requestedTeamName;
    private SportType requestedSportType;
}