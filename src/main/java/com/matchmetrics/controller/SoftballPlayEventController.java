package com.matchmetrics.controller;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.mapper.dto.ApiResponse;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/softball/play-events")
public class SoftballPlayEventController {

    private final IBaseballPlayEventService playEventService;
    private final IMatchService matchService;
    private final TeamAccessValidator teamAccessValidator;

    public SoftballPlayEventController(
            IBaseballPlayEventService playEventService,
            IMatchService matchService,
            TeamAccessValidator teamAccessValidator
    ) {
        this.playEventService = playEventService;
        this.matchService = matchService;
        this.teamAccessValidator = teamAccessValidator;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BaseballPlayEventDTO>> create(
            @Valid @RequestBody BaseballPlayEventDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to create softball play event for match: {}", dto.getMatchId());

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        MatchDTO match = matchService.getMatchById(dto.getMatchId());

        if (match.getSportType() == null || match.getSportType() != SportType.SOFTBALL) {
            throw new IllegalArgumentException("Match " + dto.getMatchId() + " is not a SOFTBALL match");
        }

        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );

        BaseballPlayEventDTO created = playEventService.createPlayEvent(dto);
        return ResponseEntity.ok(ApiResponse.ok("Softball play event created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseballPlayEventDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get softball play event: {}", id);

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
    public ResponseEntity<?> getAll(
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to list softball play events with search: {}", search);
        if (principal == null && (search == null || !search.startsWith("match:"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ok("Filter 'match:<id>' is required for public access", null));
        }
        return ResponseEntity.ok(playEventService.search(search));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BaseballPlayEventDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody BaseballPlayEventDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update softball play event: {}", id);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        BaseballPlayEventDTO existing = playEventService.getPlayEventById(id);
        MatchDTO match = matchService.getMatchById(existing.getMatchId());
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );

        BaseballPlayEventDTO updated = playEventService.updatePlayEvent(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Softball play event updated successfully", updated));
    }

    @DeleteMapping("/match/{matchId}")
    public ResponseEntity<Void> deleteByMatch(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to delete all softball play events for match: {}", matchId);

        if (principal.getRole() != UserRole.ADMIN) {
            log.warn("User {} tried to bulk-delete softball play events without ADMIN role", principal.getEmail());
            return ResponseEntity.status(403).build();
        }

        playEventService.deleteAllByMatchId(matchId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to delete softball play event: {}", id);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }

        BaseballPlayEventDTO existing = playEventService.getPlayEventById(id);
        MatchDTO match = matchService.getMatchById(existing.getMatchId());
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null
        );

        playEventService.deletePlayEvent(id);
        return ResponseEntity.noContent().build();
    }
}
