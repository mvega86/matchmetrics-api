package com.matchmetrics.mapper.dto.admin;

import com.matchmetrics.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeUserRoleRequest {

    @NotNull
    private UserRole role;
}