package com.matchmetrics.mapper.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String code;

    @NotBlank
    @Size(min = 6, message = "New password must be at least 6 characters.")
    private String newPassword;
}
