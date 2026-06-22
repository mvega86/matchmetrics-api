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

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody TournamentDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        try {
            TournamentDTO created = tournamentService.create(dto);
            return ResponseEntity.ok(Map.of("message", "Tournament created successfully", "data", created));
        } catch (Exception e) {
            log.error("Error creating tournament: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody TournamentDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        try {
            TournamentDTO updated = tournamentService.update(id, dto);
            return ResponseEntity.ok(Map.of("message", "Tournament updated successfully", "data", updated));
        } catch (Exception e) {
            log.error("Error updating tournament {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        try {
            tournamentService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting tournament {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }

    // ─── Team management ────────────────────────────────────────────────────────

    @GetMapping("/{id}/teams")
    public ResponseEntity<List<TeamDTO>> getTeams(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getTeams(id));
    }

    @PostMapping("/{id}/teams/{teamId}")
    public ResponseEntity<Map<String, Object>> addTeam(
            @PathVariable Long id,
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        try {
            TournamentDTO updated = tournamentService.addTeam(id, teamId);
            return ResponseEntity.ok(Map.of("message", "Team added to tournament", "data", updated));
        } catch (Exception e) {
            log.error("Error adding team {} to tournament {}: {}", teamId, id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/teams/{teamId}")
    public ResponseEntity<Map<String, Object>> removeTeam(
            @PathVariable Long id,
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        try {
            TournamentDTO updated = tournamentService.removeTeam(id, teamId);
            return ResponseEntity.ok(Map.of("message", "Team removed from tournament", "data", updated));
        } catch (Exception e) {
            log.error("Error removing team {} from tournament {}: {}", teamId, id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Match generation ────────────────────────────────────────────────────────

    @PostMapping("/{id}/generate")
    public ResponseEntity<Map<String, Object>> generateMatches(
            @PathVariable Long id,
            @Valid @RequestBody GenerateMatchesRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(403).build();
        }
        try {
            List<MatchDTO> matches = tournamentService.generateMatches(
                    id, request.getType(), request.getStartDate(), request.getMatchTime());
            return ResponseEntity.ok(Map.of(
                    "message", "Partidos generados: " + matches.size(),
                    "data", matches
            ));
        } catch (Exception e) {
            log.error("Error generating matches for tournament {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
