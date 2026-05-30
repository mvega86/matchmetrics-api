package com.matchmetrics.mapper.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// =========================
// DTO TeamDTO
// =========================
@Getter
@Setter
public class TeamDTO {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Acronym is required")
    private String acronym;
    @NotBlank(message = "Stadium is required")
    private String stadium;
}
