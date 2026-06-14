package com.matchmetrics.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatisticsSummaryDTO {
    private Long playerId;
    private String playerName;
    private String jerseyName;
    private Integer jerseyNumber;
    private Long teamId;
    private String teamName;
    private String teamAcronym;
    private String teamPhotoUrl;
    private String playerPhotoUrl;
    private int games;
    private int ab;
    private int hits;
    private int singles;
    private int doubles;
    private int triples;
    private int homeRuns;
    private int rbi;
    private int walks;
    private int strikeouts;
    private String avg;
    private String obp;
    private String slg;
    private String ops;
}
