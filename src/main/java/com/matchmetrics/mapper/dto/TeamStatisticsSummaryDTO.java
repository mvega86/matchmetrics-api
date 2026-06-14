package com.matchmetrics.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamStatisticsSummaryDTO {
    private Long teamId;
    private String teamName;
    private String acronym;
    private String photoUrl;
    private int games;
    private int wins;
    private int losses;
    private int ties;
    private int runsFor;
    private int runsAgainst;
    private int runDifferential;
    private String winPct;
}
