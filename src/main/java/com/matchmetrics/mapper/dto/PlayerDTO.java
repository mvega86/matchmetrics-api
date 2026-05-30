package com.matchmetrics.mapper.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO {
    private Long id;
    @NotBlank(message = "Full name is required")
    private String fullName;
    private String jerseyName;
    @Min(value = 1, message = "The dorsal must be a positive number")
    private Integer jerseyNumber;

    @Past(message = "The date of birth must be a date in the past")
    private LocalDate birthDate;
    private Integer age;
    private Long teamId;
}
