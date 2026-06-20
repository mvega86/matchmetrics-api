package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.TournamentPlayerStatsDTO;
import com.matchmetrics.service.ISoftballStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/softball/stats")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class SoftballStatsController {

    private final ISoftballStatsService softballStatsService;

    @GetMapping("/player-match/{playerMatchId}")
    public ResponseEntity<TournamentPlayerStatsDTO> getPlayerTournamentStats(
            @PathVariable Long playerMatchId
    ) {
        log.info("Request for tournament stats of playerMatch: {}", playerMatchId);
        try {
            return ResponseEntity.ok(softballStatsService.getPlayerTournamentStats(playerMatchId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/player-match/{playerMatchId}/lifetime")
    public ResponseEntity<TournamentPlayerStatsDTO> getPlayerLifetimeStats(
            @PathVariable Long playerMatchId
    ) {
        log.info("Request for lifetime stats of playerMatch: {}", playerMatchId);
        try {
            return ResponseEntity.ok(softballStatsService.getPlayerLifetimeStats(playerMatchId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
