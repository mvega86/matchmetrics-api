package com.matchmetrics.controller;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.mapper.dto.BaseballPlayEventDTO;
import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.security.TeamAccessValidator;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IBaseballPlayEventService;
import com.matchmetrics.service.IMatchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/baseball/play-events")
public class BaseballPlayEventController {

    private final IBaseballPlayEventService playEventService;
    private final IMatchService matchService;
    private final TeamAccessValidator teamAccessValidator;

    public BaseballPlayEventController(
            IBaseballPlayEventService playEventService,
            IMatchService matchService,
            TeamAccessValidator teamAccessValidator
    ) {
        this.playEventService = playEventService;
        this.matchService = matchService;
        this.teamAccessValidator = teamAccessValidator;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody BaseballPlayEventDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to create play event for match: {}", dto.getMatchId());

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            log.warn("User {} tried to create play event without permission", principal.getEmail());
            return ResponseEntity.status(403).build();
        }

        MatchDTO match = matchService.getMatchById(dto.getMatchId());
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );

        try {
            BaseballPlayEventDTO created = playEventService.createPlayEvent(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Play event created successfully",
                    "data", created
            ));
        } catch (Exception e) {
            log.error("Error creating play event: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseballPlayEventDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get play event: {}", id);

        BaseballPlayEventDTO event = playEventService.getPlayEventById(id);
        MatchDTO match = matchService.getMatchById(event.getMatchId());
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<BaseballPlayEventDTO>> getAll(
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to list play events with search: {}", search);
        return ResponseEntity.ok(playEventService.search(search));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody BaseballPlayEventDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update play event: {}", id);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            log.warn("User {} tried to update play event without permission", principal.getEmail());
            return ResponseEntity.status(403).build();
        }

        BaseballPlayEventDTO existing = playEventService.getPlayEventById(id);
        MatchDTO match = matchService.getMatchById(existing.getMatchId());
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );

        try {
            BaseballPlayEventDTO updated = playEventService.updatePlayEvent(id, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Play event updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating play event {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/match/{matchId}")
    public ResponseEntity<Void> deleteByMatch(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to delete all play events for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN) {
            log.warn("User {} tried to bulk-delete play events without ADMIN role", principal.getEmail());
            return ResponseEntity.status(403).build();
        }

        try {
            playEventService.deleteAllByMatchId(matchId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting play events for match {}: {}", matchId, e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to delete play event: {}", id);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            log.warn("User {} tried to delete play event without permission", principal.getEmail());
            return ResponseEntity.status(403).build();
        }

        BaseballPlayEventDTO existing = playEventService.getPlayEventById(id);
        MatchDTO match = matchService.getMatchById(existing.getMatchId());
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );

        try {
            playEventService.deletePlayEvent(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting play event {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }
}
