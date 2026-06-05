package com.matchmetrics.mapper.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GenerateMatchesRequestDTO {

    @NotBlank(message = "Type is required (ROUND_ROBIN or ELIMINATION)")
    private String type;

    private LocalDate startDate;

    private String matchTime = "15:00";
}
