package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.BaseballGameStateDTO;
import com.matchmetrics.security.TeamAccessValidator;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IBaseballGameStateService;
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
@CrossOrigin(origins = "http://localhost:5173")
public class BaseballGameStateController {

    private final IBaseballGameStateService gameStateService;
    private final TeamAccessValidator teamAccessValidator;

    public BaseballGameStateController(
            IBaseballGameStateService gameStateService,
            TeamAccessValidator teamAccessValidator
    ) {
        this.gameStateService = gameStateService;
        this.teamAccessValidator = teamAccessValidator;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createGameState(
            @Valid @RequestBody BaseballGameStateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to create game state for match: {}", dto.getMatchId());

        // Only ADMIN and MANAGER can create game state
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            log.warn("User {} tried to create game state without permission", principal.getEmail());
            return ResponseEntity.status(403).build();
        }

        // MANAGER must validate team access
        if (principal.getRole() == UserRole.MANAGER) {
            // Validate team access would be done when getting the match
            log.info("MANAGER {} creating game state", principal.getEmail());
        }

        try {
            BaseballGameStateDTO created = gameStateService.createGameState(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Game state created successfully",
                    "data", created
            ));
        } catch (Exception e) {
            log.error("Error creating game state: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<BaseballGameStateDTO> getGameState(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get game state for match: {}", matchId);

        try {
            BaseballGameStateDTO gameState = gameStateService.getGameStateByMatchId(matchId);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Error retrieving game state: {}", e.getMessage());
            return ResponseEntity.status(404).build();
        }
    }

    @PutMapping("/{matchId}/inning")
    public ResponseEntity<Map<String, Object>> updateInning(
            @PathVariable Long matchId,
            @RequestParam Integer inning,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update inning for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballGameStateDTO updated = gameStateService.updateInning(matchId, inning);
            return ResponseEntity.ok(Map.of(
                    "message", "Inning updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating inning: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{matchId}/inning-half")
    public ResponseEntity<Map<String, Object>> updateInningHalf(
            @PathVariable Long matchId,
            @RequestParam String inningHalf,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update inning half for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballGameStateDTO updated = gameStateService.updateInningHalf(matchId, inningHalf);
            return ResponseEntity.ok(Map.of(
                    "message", "Inning half updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating inning half: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{matchId}/outs")
    public ResponseEntity<Map<String, Object>> updateOuts(
            @PathVariable Long matchId,
            @RequestParam Integer outs,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update outs for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballGameStateDTO updated = gameStateService.updateOuts(matchId, outs);
            return ResponseEntity.ok(Map.of(
                    "message", "Outs updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating outs: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{matchId}/balls")
    public ResponseEntity<Map<String, Object>> updateBalls(
            @PathVariable Long matchId,
            @RequestParam Integer balls,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update balls for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballGameStateDTO updated = gameStateService.updateBalls(matchId, balls);
            return ResponseEntity.ok(Map.of(
                    "message", "Balls updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating balls: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{matchId}/strikes")
    public ResponseEntity<Map<String, Object>> updateStrikes(
            @PathVariable Long matchId,
            @RequestParam Integer strikes,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update strikes for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballGameStateDTO updated = gameStateService.updateStrikes(matchId, strikes);
            return ResponseEntity.ok(Map.of(
                    "message", "Strikes updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating strikes: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{matchId}/bases")
    public ResponseEntity<Map<String, Object>> updateBases(
            @PathVariable Long matchId,
            @RequestParam(required = false) Long firstBasePlayerId,
            @RequestParam(required = false) Long secondBasePlayerId,
            @RequestParam(required = false) Long thirdBasePlayerId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update bases for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballGameStateDTO updated = gameStateService.updateBases(matchId, firstBasePlayerId, secondBasePlayerId, thirdBasePlayerId);
            return ResponseEntity.ok(Map.of(
                    "message", "Bases updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating bases: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{matchId}/score")
    public ResponseEntity<Map<String, Object>> updateScore(
            @PathVariable Long matchId,
            @RequestParam Integer homeScore,
            @RequestParam Integer awayScore,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update score for match: {} - Home: {}, Away: {}", matchId, homeScore, awayScore);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballGameStateDTO updated = gameStateService.updateScore(matchId, homeScore, awayScore);
            return ResponseEntity.ok(Map.of(
                    "message", "Score updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating score: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{matchId}/finish")
    public ResponseEntity<Map<String, Object>> finishGame(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to finish game for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballGameStateDTO updated = gameStateService.finishGame(matchId);
            return ResponseEntity.ok(Map.of(
                    "message", "Game finished successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error finishing game: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> deleteGameState(
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
