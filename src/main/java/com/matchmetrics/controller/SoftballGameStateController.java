package com.matchmetrics.controller;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.mapper.dto.BaseballGameStateDTO;
import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.security.TeamAccessValidator;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IBaseballGameStateService;
import com.matchmetrics.service.IMatchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/softball/game-state")
@CrossOrigin(origins = "http://localhost:5173")
public class SoftballGameStateController {

    private final IBaseballGameStateService gameStateService;
    private final IMatchService matchService;
    private final TeamAccessValidator teamAccessValidator;

    public SoftballGameStateController(
            IBaseballGameStateService gameStateService,
            IMatchService matchService,
            TeamAccessValidator teamAccessValidator
    ) {
        this.gameStateService = gameStateService;
        this.matchService = matchService;
        this.teamAccessValidator = teamAccessValidator;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(
            @Valid @RequestBody BaseballGameStateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to save softball game state for match: {}", dto.getMatchId());

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        MatchDTO match = matchService.getMatchById(dto.getMatchId());
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );

        try {
            BaseballGameStateDTO created = gameStateService.createGameState(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Softball game state saved successfully",
                    "data", created
            ));
        } catch (Exception e) {
            log.error("Error saving softball game state: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<BaseballGameStateDTO> get(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get softball game state for match: {}", matchId);

        MatchDTO match = matchService.getMatchById(matchId);
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );

        try {
            BaseballGameStateDTO gameState = gameStateService.getGameStateByMatchId(matchId);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Error retrieving softball game state: {}", e.getMessage());
            return ResponseEntity.status(404).build();
        }
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long matchId,
            @RequestBody BaseballGameStateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update softball game state for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        MatchDTO match = matchService.getMatchById(matchId);
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );

        try {
            BaseballGameStateDTO updated = gameStateService.updateGameState(matchId, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Softball game state updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating softball game state: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to delete softball game state for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        try {
            gameStateService.deleteGameState(matchId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting softball game state: {}", e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }
}
