package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.service.ITeamService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// =========================
// CONTROLADOR TeamController
// =========================
@Slf4j
//@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private ITeamService teamService;

    public TeamController(ITeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<TeamDTO>> getAll(
            @RequestParam(value = "search", required = false) String search
    ) {
        log.info("Request received to search teams with search: {}", search);
        return ResponseEntity.ok(teamService.search(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getById(@PathVariable Long id) {
        log.debug("Request received to search team with ID: {}", id);
        TeamDTO team = teamService.getById(id);
        return team != null ? ResponseEntity.ok(team) : ResponseEntity.notFound().build();
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
