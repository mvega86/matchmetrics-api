package com.matchmetrics.controller;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.mapper.dto.TeamStatisticsSummaryDTO;
import com.matchmetrics.service.ITeamStatisticsQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/team-stats")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class TeamStatsController {

    private final ITeamStatisticsQueryService teamStatsService;

    @GetMapping
    public ResponseEntity<List<TeamStatisticsSummaryDTO>> getTeamStatsList(
            @RequestParam(value = "sportType", defaultValue = "SOFTBALL") String sportType,
            @RequestParam(value = "tournamentId", required = false) Long tournamentId
    ) {
        log.info("Team stats list: sportType={}, tournamentId={}", sportType, tournamentId);
        SportType sport = SportType.valueOf(sportType.toUpperCase());
        return ResponseEntity.ok(teamStatsService.getTeamStatsList(sport, tournamentId));
    }
}
