package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.TournamentPlayerStatsDTO;

public interface ISoftballStatsService {
    TournamentPlayerStatsDTO getPlayerTournamentStats(Long playerMatchId);
    TournamentPlayerStatsDTO getPlayerLifetimeStats(Long playerMatchId);
}
