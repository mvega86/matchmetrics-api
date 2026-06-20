package com.matchmetrics.service;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.mapper.dto.PlayerStatisticsDetailDTO;
import com.matchmetrics.mapper.dto.PlayerStatisticsSummaryDTO;

import java.util.List;

public interface IPlayerStatisticsQueryService {
    List<PlayerStatisticsSummaryDTO> getPlayerStatsList(SportType sportType, Long teamId, Long tournamentId);
    PlayerStatisticsDetailDTO getPlayerStatsDetail(Long playerId, Long tournamentId);
}
