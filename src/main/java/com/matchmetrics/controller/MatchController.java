package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.service.IMatchService;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.security.TeamAccessValidator;
import com.matchmetrics.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/matches")
@Slf4j
public class MatchController {

    private final IMatchService matchService;
    private final TeamAccessValidator teamAccessValidator;

    public MatchController(
            IMatchService matchService,
            TeamAccessValidator teamAccessValidator
    ) {
        this.matchService = matchService;
        this.teamAccessValidator = teamAccessValidator;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createMatch(@Valid @RequestBody MatchDTO matchDTO) {
        log.info("Request to create match: {}", matchDTO);
        MatchDTO saved = matchService.createMatch(matchDTO);
        return ResponseEntity.ok(Map.of(
                "message", "Successfully saved match!!!",
                "data", saved
        ));
    }

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAllMatches(
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to fetch matches with search: {}", search);

        if (principal.getRole() == UserRole.ADMIN) {
            return ResponseEntity.ok(matchService.search(search));
        }

        if (principal.getTeamId() == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                matchService.searchByTeam(search, principal.getTeamId())
        );
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDTO> getMatchById(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to fetch match with ID: {}", matchId);

        MatchDTO matchDTO = matchService.getMatchById(matchId);

        Long homeTeamId = matchDTO.getHomeTeam() != null
                ? matchDTO.getHomeTeam().getId()
                : null;

        Long awayTeamId = matchDTO.getAwayTeam() != null
                ? matchDTO.getAwayTeam().getId()
                : null;

        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                homeTeamId,
                awayTeamId
        );

        return ResponseEntity.ok(matchDTO);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateMatch(@RequestBody MatchDTO matchDTO) {
        log.info("Request to update match: {}", matchDTO.getHomeTeam()+" VS "+matchDTO.getAwayTeam());
        MatchDTO updated = matchService.updateMatch(matchDTO);
        log.info("Match updated.");
        return ResponseEntity.ok(Map.of(
                "message", "Successfully updated match!!!",
                "data", updated
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Request received to delete match with ID: {}", id);
        matchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
