package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.PlayerMatchDTO;
import com.matchmetrics.service.IPlayerMatchService;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.security.TeamAccessValidator;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/players-match")
@Slf4j
public class PlayerMatchController {

    private final IPlayerMatchService playerMatchService;
    private final TeamAccessValidator teamAccessValidator;

    public PlayerMatchController(
            IPlayerMatchService playerMatchService,
            TeamAccessValidator teamAccessValidator
    ) {
        this.playerMatchService = playerMatchService;
        this.teamAccessValidator = teamAccessValidator;
    }

    @GetMapping
    public ResponseEntity<List<PlayerMatchDTO>> getAll(
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Logger: Request to get all players match with search: {}", search);

        if (principal.getRole() == UserRole.ADMIN) {
            return ResponseEntity.ok(playerMatchService.search(search));
        }

        if (principal.getTeamId() == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                playerMatchService.searchByTeam(search, principal.getTeamId())
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(
            @Valid @RequestBody PlayerMatchDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                dto.getPlayer() != null ? dto.getPlayer().getTeamId() : null
        );

        log.info("Logger: Assigning player {} to match {}", dto.getPlayer().getId(), dto.getMatch().getId());
        PlayerMatchDTO saved = playerMatchService.save(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Player match assigned successfully");
        response.put("data", saved);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerMatchDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Logger: Request to fetch player match with ID: {}", id);

        PlayerMatchDTO playerMatchDTO = playerMatchService.getById(id);

        Long resourceTeamId = playerMatchDTO.getPlayer() != null
                ? playerMatchDTO.getPlayer().getTeamId()
                : null;

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                resourceTeamId
        );

        return ResponseEntity.ok(playerMatchDTO);
    }

    @PutMapping()
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody PlayerMatchDTO playerMatchDTO,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PlayerMatchDTO currentPlayerMatch = playerMatchService.getById(playerMatchDTO.getId());

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                currentPlayerMatch.getPlayer() != null ? currentPlayerMatch.getPlayer().getTeamId() : null
        );

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                playerMatchDTO.getPlayer() != null ? playerMatchDTO.getPlayer().getTeamId() : null
        );

        log.info("Logger: Request to update playerMatch, ID: {}", playerMatchDTO.getId());
        PlayerMatchDTO playerMatchDTO1 = playerMatchService.updatePlayerMatch(playerMatchDTO);

        return ResponseEntity.ok(Map.of(
                "message", "Player match updated successfully!!!",
                "data", playerMatchDTO1
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PlayerMatchDTO playerMatchDTO = playerMatchService.getById(id);

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                playerMatchDTO.getPlayer() != null ? playerMatchDTO.getPlayer().getTeamId() : null
        );

        log.debug("Logger: Request received to delete player match with ID: {}", id);
        playerMatchService.delete(id);

        return ResponseEntity.noContent().build();
    }

}

