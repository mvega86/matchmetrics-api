package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.PlayerDTO;
import com.matchmetrics.mapper.dto.PlayerPublicDTO;
import com.matchmetrics.security.TeamAccessValidator;
import com.matchmetrics.service.IPlayerService;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.security.UserPrincipal;

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

@Slf4j
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
    public ResponseEntity<?> getAll(
            @RequestParam(value = "search", required = false) String search,
            @PageableDefault(size = 1000, sort = "fullName") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get all players with search: {}", search);

        if (principal == null) {
            return ResponseEntity.ok(
                playerService.searchPlayersPage(search, pageable).map(PlayerPublicDTO::from));
        }

        if (principal.getRole() == UserRole.ADMIN) {
            return ResponseEntity.ok(playerService.searchPlayersPage(search, pageable));
        }

        if (principal.getTeamId() == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(playerService.searchPlayersByTeamPage(search, principal.getTeamId(), pageable));
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
    public ResponseEntity<ApiResponse<PlayerDTO>> save(
            @Valid @RequestBody PlayerDTO playerDTO,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                playerDTO.getTeamId()
        );

        log.info("Request received to save player: {}", playerDTO.getFullName());
        PlayerDTO saved = playerService.save(playerDTO);

        return ResponseEntity.ok(ApiResponse.ok("Successfully saved player!!!", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PlayerDTO player = playerService.getById(id);

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                player.getTeamId()
        );

        log.debug("Request received to delete player with ID: {}", id);
        playerService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PlayerDTO>> update(
            @Valid @RequestBody PlayerDTO playerDTO,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PlayerDTO currentPlayer = playerService.getById(playerDTO.getId());

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                currentPlayer.getTeamId()
        );

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                playerDTO.getTeamId()
        );

        log.info("Request to update player...");
        PlayerDTO playerDTOOut = playerService.updateStatistic(playerDTO);

        return ResponseEntity.ok(ApiResponse.ok("Successfully updated player!!!", playerDTOOut));
    }
}
