package com.matchmetrics.mapper.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendResetCodeRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String method;
}
