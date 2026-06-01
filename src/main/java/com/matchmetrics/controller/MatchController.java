package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.service.IMatchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/matches")
@Slf4j
public class MatchController {

    private final IMatchService matchService;

    public MatchController(IMatchService matchService) {
        this.matchService = matchService;
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
            @RequestParam(value = "search", required = false) String search
    ) {
        log.info("Request to fetch matches with search: {}", search);
        return ResponseEntity.ok(matchService.search(search));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDTO> getMatchById(@PathVariable Long matchId) {
        log.info("Request to fetch match with ID: {}", matchId);
        MatchDTO matchDTO = matchService.getMatchById(matchId);
        return matchDTO != null ? ResponseEntity.ok(matchDTO) : ResponseEntity.notFound().build();
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
