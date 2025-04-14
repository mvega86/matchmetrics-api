package com.futbol.api_party.mapper.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PlayerStatisticDTO {

    private Long id;

    @NotNull(message = "PlayerMatch cannot be null.")
    private PlayerMatchDTO playerMatch;

    @NotNull(message = "The statistic is required.")
    private StatisticDTO statistic;

    @NotNull(message = "The statistics timestamp is required.")
    private LocalDateTime timestamp;

    private Double positionX;
    private Double positionY;
    private String observation;
    private FieldZoneDTO fieldZone;

    private String relativeMinuteFormatted; //Relative minute the statistic occurred
}
