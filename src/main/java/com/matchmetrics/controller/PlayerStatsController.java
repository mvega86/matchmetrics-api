package com.matchmetrics.controller;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.mapper.dto.PlayerStatisticsDetailDTO;
import com.matchmetrics.mapper.dto.PlayerStatisticsSummaryDTO;
import com.matchmetrics.service.IPlayerStatisticsQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/player-stats")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PlayerStatsController {

    private final IPlayerStatisticsQueryService playerStatsService;

    @GetMapping
    public ResponseEntity<List<PlayerStatisticsSummaryDTO>> getPlayerStatsList(
            @RequestParam(value = "sportType", defaultValue = "SOFTBALL") String sportType,
            @RequestParam(value = "teamId", required = false) Long teamId,
            @RequestParam(value = "tournamentId", required = false) Long tournamentId
    ) {
        log.info("Player stats list: sportType={}, teamId={}, tournamentId={}", sportType, teamId, tournamentId);
        SportType sport = SportType.valueOf(sportType.toUpperCase());
        return ResponseEntity.ok(playerStatsService.getPlayerStatsList(sport, teamId, tournamentId));
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerStatisticsDetailDTO> getPlayerStatsDetail(
            @PathVariable Long playerId,
            @RequestParam(value = "tournamentId", required = false) Long tournamentId
    ) {
        log.info("Player stats detail: playerId={}, tournamentId={}", playerId, tournamentId);
        return ResponseEntity.ok(playerStatsService.getPlayerStatsDetail(playerId, tournamentId));
    }
}
