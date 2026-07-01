package com.matchmetrics.mapper.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemMetricsDTO {
    private long totalUsers;
    private long totalMatches;
    private long activeLiveMatches;
    private long totalPlayEvents;
    private double playEventTableSizeMB;
    private long totalPlayers;
    private long totalTeams;
    private long totalTournaments;
    private int currentPhase;
    private String phaseLabel;
    private String phaseDescription;
    private String nextPhaseThreshold;
}
