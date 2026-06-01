package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.PlayerDTO;
import com.matchmetrics.security.TeamAccessValidator;
import com.matchmetrics.service.IPlayerService;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.security.UserPrincipal;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;

// =========================
// CONTROLADOR PlayerController
// =========================
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private IPlayerService playerService;
    private TeamAccessValidator teamAccessValidator;

    public PlayerController(IPlayerService playerService, TeamAccessValidator teamAccessValidator) {
        this.playerService = playerService;
        this.teamAccessValidator = teamAccessValidator;
    }

    @GetMapping
    public ResponseEntity<List<PlayerDTO>> getAll(
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get all players with search: {}", search);

        if (principal.getRole() == UserRole.ADMIN) {
            List<PlayerDTO> playerDTOList = playerService.searchPlayers(search);
            return ResponseEntity.ok(playerDTOList);
        }

        if (principal.getTeamId() == null) {
            return ResponseEntity.status(403).build();
        }

        List<PlayerDTO> playerDTOList = playerService.searchPlayersByTeam(
                search,
                principal.getTeamId()
        );

        return ResponseEntity.ok(playerDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PlayerDTO player = playerService.getById(id);

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                player.getTeamId()
        );

        return ResponseEntity.ok(player);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody PlayerDTO playerDTO) {
        log.info("Request received to save player: {}", playerDTO.getFullName());
        PlayerDTO saved = playerService.save(playerDTO);
        return ResponseEntity.ok(Map.of(
                "message", "Successfully saved player!!!",
                "data", saved
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Request received to delete player with ID: {}", id);
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody PlayerDTO playerDTO) {
        log.info("Request to update player...");
        PlayerDTO playerDTOOut = playerService.updateStatistic(playerDTO);
        log.info("Player updated.");
        return ResponseEntity.ok(Map.of(
                "message", "Successfully updated player!!!",
                "data", playerDTOOut
        ));
    }
}
