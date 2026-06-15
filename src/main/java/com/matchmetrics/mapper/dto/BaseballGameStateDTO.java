package com.matchmetrics.mapper.dto;

import com.matchmetrics.domain.enums.BaseballGameStatus;
import com.matchmetrics.domain.enums.InningHalf;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseballGameStateDTO {
    private Long id;

    @NotNull(message = "Match is required.")
    private Long matchId;

    @NotNull(message = "Current inning is required.")
    @Min(value = 1, message = "Current inning must be at least 1.")
    private Integer currentInning;

    @NotNull(message = "Inning half is required.")
    private InningHalf inningHalf;

    @Min(value = 0, message = "Outs cannot be negative.")
    private Integer outs;

    @Min(value = 0, message = "Balls cannot be negative.")
    private Integer balls;

    @Min(value = 0, message = "Strikes cannot be negative.")
    private Integer strikes;

    @Min(value = 0, message = "Home score cannot be negative.")
    private Integer homeScore;

    @Min(value = 0, message = "Away score cannot be negative.")
    private Integer awayScore;

    private Long firstBasePlayerMatchId;
    private Long secondBasePlayerMatchId;
    private Long thirdBasePlayerMatchId;
    private Long currentBatterPlayerMatchId;

    private Boolean clearFirstBase;
    private Boolean clearCurrentBatter;
    private Boolean clearSecondBase;
    private Boolean clearThirdBase;

    private Integer pitchCount;

    private Long currentPitcherPlayerMatchId;

    private Map<Long, Integer> pitcherPitchCounts;

    @NotNull(message = "Status is required.")
    private BaseballGameStatus status;

    private Integer totalInnings;
}
