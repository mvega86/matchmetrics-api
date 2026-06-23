package com.matchmetrics.mapper.dto;

import com.matchmetrics.domain.enums.BaseballEventType;
import com.matchmetrics.domain.enums.InningHalf;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseballPlayEventDTO {

    private Long id;

    @NotNull(message = "Match is required.")
    private Long matchId;

    @NotNull(message = "Inning is required.")
    @Min(value = 1, message = "Inning must be at least 1.")
    private Integer inning;

    @NotNull(message = "Inning half is required.")
    private InningHalf inningHalf;

    @NotNull(message = "Event type is required.")
    private BaseballEventType eventType;

    @NotNull(message = "Batting team is required.")
    private Long battingTeamId;

    @NotNull(message = "Fielding team is required.")
    private Long fieldingTeamId;

    private Long batterPlayerMatchId;
    private Long pitcherPlayerMatchId;
    private String result;
    @Min(value = 0, message = "Runs scored cannot be negative.")
    private Integer runsScored;
    @Min(value = 0, message = "Outs on play cannot be negative.")
    private Integer outsOnPlay;
    @Min(value = 0, message = "RBI cannot be negative.")
    private Integer rbi;
    private String description;
    private Integer ballsBefore;
    private Integer strikesBefore;
    private Integer outsBefore;
    private Boolean firstBaseBefore;
    private Boolean secondBaseBefore;
    private Boolean thirdBaseBefore;
    private LocalDateTime createdAt;
}
