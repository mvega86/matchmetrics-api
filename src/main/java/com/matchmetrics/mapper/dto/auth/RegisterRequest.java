package com.matchmetrics.mapper.dto.auth;

import com.matchmetrics.domain.enums.SportType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private Long teamId;

    private String requestedTeamName;

    private SportType requestedSportType;
}