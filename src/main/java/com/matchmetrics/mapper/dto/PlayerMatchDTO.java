package com.matchmetrics.mapper.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PlayerMatchDTO {

    private Long id;

    @NotNull(message = "Match is required.")
    private MatchDTO match;

    @NotNull(message = "Player is required.")
    private PlayerDTO player;

    private LocalDateTime inTime;
    private LocalDateTime outTime;

    private String inMinuteFormatted;
    private String outMinuteFormatted;

    private Integer battingOrder;
    private String fieldPosition;
}

