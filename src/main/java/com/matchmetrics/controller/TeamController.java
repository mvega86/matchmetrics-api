package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.service.ITeamService;
import com.matchmetrics.security.TeamAccessValidator;
import com.matchmetrics.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;

// =========================
// CONTROLADOR TeamController
// =========================
@Slf4j
@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private ITeamService teamService;
    private final TeamAccessValidator teamAccessValidator;

    public TeamController(ITeamService teamService, TeamAccessValidator teamAccessValidator) {
        this.teamService = teamService;
        this.teamAccessValidator = teamAccessValidator;
    }

    @GetMapping
    public ResponseEntity<List<TeamDTO>> getAll(
            @RequestParam(value = "search", required = false) String search
    ) {
        log.info("Request received to search teams with search: {}", search);
        return ResponseEntity.ok(teamService.search(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TeamDTO team = teamService.getById(id);

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                team.getId()
        );

        return ResponseEntity.ok(team);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody TeamDTO teamDTO) {
        log.info("Request received to save team: {}", teamDTO.getName());
        TeamDTO saved = teamService.save(teamDTO);
        return ResponseEntity.ok(Map.of(
                "message", "Successfully saved team!!!",
                "data", saved
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Request received to delete team with ID: {}", id);
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody TeamDTO teamDTO) {
        log.info("Request to update team...");
        TeamDTO teamDTOOut = teamService.updateTeam(teamDTO);
        log.info("Team updated.");
        return ResponseEntity.ok(Map.of(
                "message", "Successfully updated team!!!",
                "data", teamDTOOut
        ));
    }
}
