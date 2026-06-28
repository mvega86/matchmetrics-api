package com.matchmetrics.controller;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.mapper.dto.GenerateMatchesRequestDTO;
import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.mapper.dto.TournamentDTO;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.ITournamentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.matchmetrics.mapper.dto.ApiResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tournaments")
public class TournamentController {

    private final ITournamentService tournamentService;

    public TournamentController(ITournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> getAll(
            @RequestParam(value = "search", required = false) String search
    ) {
        return ResponseEntity.ok(tournamentService.search(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TournamentDTO>> create(
            @Valid @RequestBody TournamentDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        TournamentDTO created = tournamentService.create(dto);
        return ResponseEntity.ok(ApiResponse.ok("Tournament created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TournamentDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody TournamentDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        TournamentDTO updated = tournamentService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Tournament updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        tournamentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Team management ────────────────────────────────────────────────────────

    @GetMapping("/{id}/teams")
    public ResponseEntity<List<TeamDTO>> getTeams(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getTeams(id));
    }

    @PostMapping("/{id}/teams/{teamId}")
    public ResponseEntity<ApiResponse<TournamentDTO>> addTeam(
            @PathVariable Long id,
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        TournamentDTO updated = tournamentService.addTeam(id, teamId);
        return ResponseEntity.ok(ApiResponse.ok("Team added to tournament", updated));
    }

    @DeleteMapping("/{id}/teams/{teamId}")
    public ResponseEntity<ApiResponse<TournamentDTO>> removeTeam(
            @PathVariable Long id,
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        TournamentDTO updated = tournamentService.removeTeam(id, teamId);
        return ResponseEntity.ok(ApiResponse.ok("Team removed from tournament", updated));
    }

    // ─── Match generation ────────────────────────────────────────────────────────

    @PostMapping("/{id}/generate")
    public ResponseEntity<ApiResponse<List<MatchDTO>>> generateMatches(
            @PathVariable Long id,
            @Valid @RequestBody GenerateMatchesRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        List<MatchDTO> matches = tournamentService.generateMatches(
                id, request.getType(), request.getStartDate(), request.getMatchTime());
        return ResponseEntity.ok(ApiResponse.ok("Partidos generados: " + matches.size(), matches));
    }
}
