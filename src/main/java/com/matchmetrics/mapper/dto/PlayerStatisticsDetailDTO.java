package com.matchmetrics.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatisticsDetailDTO {
    private Long playerId;
    private String playerName;
    private String jerseyName;
    private Integer jerseyNumber;
    private Long teamId;
    private String teamName;
    private String teamAcronym;
    private String teamPhotoUrl;
    private String playerPhotoUrl;
    private TournamentPlayerStatsDTO lifetimeStats;
    private List<TournamentBreakdownDTO> tournamentBreakdown;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TournamentBreakdownDTO {
        private Long tournamentId;
        private String tournamentName;
        private TournamentPlayerStatsDTO stats;
    }
}
