package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.service.IMatchService;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.security.TeamAccessValidator;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.matchmetrics.mapper.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

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
    public ResponseEntity<ApiResponse<MatchDTO>> save(
            @Valid @RequestBody MatchDTO matchDTO,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                matchDTO.getHomeTeam() != null ? matchDTO.getHomeTeam().getId() : null,
                matchDTO.getAwayTeam() != null ? matchDTO.getAwayTeam().getId() : null
        );

        log.info("Request to create match: {}", matchDTO);
        MatchDTO saved = matchService.createMatch(matchDTO);

        return ResponseEntity.ok(ApiResponse.ok("Successfully saved match!!!", saved));
    }

    @GetMapping
    public ResponseEntity<Page<MatchDTO>> getAll(
            @RequestParam(value = "search", required = false) String search,
            @PageableDefault(size = 1000, sort = "startFirstTime") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to fetch matches with search: {}", search);
        return ResponseEntity.ok(matchService.searchPage(search, pageable));
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
    public ResponseEntity<ApiResponse<MatchDTO>> update(
            @Valid @RequestBody MatchDTO matchDTO,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MatchDTO currentMatch = matchService.getMatchById(matchDTO.getId());

        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                currentMatch.getHomeTeam() != null ? currentMatch.getHomeTeam().getId() : null,
                currentMatch.getAwayTeam() != null ? currentMatch.getAwayTeam().getId() : null
        );

        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                matchDTO.getHomeTeam() != null ? matchDTO.getHomeTeam().getId() : null,
                matchDTO.getAwayTeam() != null ? matchDTO.getAwayTeam().getId() : null
        );

        log.info("Request to update match: {}", matchDTO.getHomeTeam() + " VS " + matchDTO.getAwayTeam());
        MatchDTO updated = matchService.updateMatch(matchDTO);

        return ResponseEntity.ok(ApiResponse.ok("Successfully updated match!!!", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MatchDTO matchDTO = matchService.getMatchById(id);

        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                matchDTO.getHomeTeam() != null ? matchDTO.getHomeTeam().getId() : null,
                matchDTO.getAwayTeam() != null ? matchDTO.getAwayTeam().getId() : null
        );

        log.debug("Request received to delete match with ID: {}", id);
        matchService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
