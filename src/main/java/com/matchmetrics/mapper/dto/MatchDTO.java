package com.matchmetrics.mapper.dto;

import com.matchmetrics.domain.enums.MatchPhase;
import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.domain.enums.SportType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MatchDTO {

    private Long id;

    private SportType sportType;

    @NotNull(message = "Location match is required.")
    private String location;

    @NotNull(message = "State is required.")
    private MatchState state;

    @NotNull(message = "Phase is required.")
    private MatchPhase phase;

    @NotNull(message = "Home team is required.")
    private TeamDTO homeTeam;

    @NotNull(message = "Away team is required.")
    private TeamDTO awayTeam;

    @NotNull(message = "The date and time of the match is required.")
    @FutureOrPresent(message = "The date of the match cannot be earlier than the current time.")
    private LocalDateTime startFirstTime;
    private LocalDateTime endFirstTime;
    private LocalDateTime startSecondTime;
    private LocalDateTime endSecondTime;
    private LocalDateTime startFirstExtraTime;
    private LocalDateTime endFirstExtraTime;
    private LocalDateTime startSecondExtraTime;
    private LocalDateTime endSecondExtraTime;
}

