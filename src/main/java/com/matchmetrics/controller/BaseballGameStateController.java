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

import com.matchmetrics.mapper.dto.ApiResponse;

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
    public ResponseEntity<ApiResponse<BaseballGameStateDTO>> save(
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

        BaseballGameStateDTO created = gameStateService.createGameState(dto);
        return ResponseEntity.ok(ApiResponse.ok("Game state saved successfully", created));
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

        return ResponseEntity.ok(gameStateService.getGameStateByMatchId(matchId));
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<ApiResponse<BaseballGameStateDTO>> update(
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

        BaseballGameStateDTO updated = gameStateService.updateGameState(matchId, dto);
        return ResponseEntity.ok(ApiResponse.ok("Game state updated successfully", updated));
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

        gameStateService.deleteGameState(matchId);
        return ResponseEntity.noContent().build();
    }
}
