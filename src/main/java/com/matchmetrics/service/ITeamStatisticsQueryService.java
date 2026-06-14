package com.matchmetrics.service;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.mapper.dto.TeamStatisticsSummaryDTO;

import java.util.List;

public interface ITeamStatisticsQueryService {
    List<TeamStatisticsSummaryDTO> getTeamStatsList(SportType sportType, Long tournamentId);
}
