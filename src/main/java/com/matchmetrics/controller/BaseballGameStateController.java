package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.BaseballGameStateDTO;
import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.security.TeamAccessValidator;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IBaseballGameStateService;
import com.matchmetrics.service.IMatchService;
import com.matchmetrics.domain.enums.UserRole;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/baseball/game-state")
public class BaseballGameStateController {

    private final IBaseballGameStateService gameStateService;
    private final IMatchService matchService;
    private final TeamAccessValidator teamAccessValidator;

    public BaseballGameStateController(
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
        log.info("Request to save game state for match: {}", dto.getMatchId());

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            log.warn("User {} tried to save game state without permission", principal.getEmail());
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
                    "message", "Game state saved successfully",
                    "data", created
            ));
        } catch (Exception e) {
            log.error("Error saving game state: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<BaseballGameStateDTO> get(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get game state for match: {}", matchId);

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
            log.error("Error retrieving game state: {}", e.getMessage());
            return ResponseEntity.status(404).build();
        }
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long matchId,
            @Valid @RequestBody BaseballGameStateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update game state for match: {}", matchId);

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
                    "message", "Game state updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating game state: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to delete game state for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        try {
            gameStateService.deleteGameState(matchId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting game state: {}", e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }
}
