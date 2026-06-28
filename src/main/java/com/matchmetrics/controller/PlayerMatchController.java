package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.mapper.dto.PlayerDTO;
import com.matchmetrics.mapper.dto.PlayerMatchDTO;
import com.matchmetrics.service.IMatchService;
import com.matchmetrics.service.IPlayerMatchService;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.security.TeamAccessValidator;

import com.matchmetrics.service.IPlayerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.matchmetrics.mapper.dto.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players-match")
@Slf4j
public class PlayerMatchController {

    private final IPlayerMatchService playerMatchService;
    private final IPlayerService playerService;
    private final IMatchService matchService;
    private final TeamAccessValidator teamAccessValidator;

    public PlayerMatchController(
            IPlayerMatchService playerMatchService, IPlayerService playerService, IMatchService matchService,
            TeamAccessValidator teamAccessValidator
    ) {
        this.playerMatchService = playerMatchService;
        this.playerService = playerService;
        this.matchService = matchService;
        this.teamAccessValidator = teamAccessValidator;
    }

    @GetMapping
    public ResponseEntity<List<PlayerMatchDTO>> getAll(
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get all players match with search: {}", search);

        if (principal == null || principal.getRole() == UserRole.ADMIN) {
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
    public ResponseEntity<ApiResponse<PlayerMatchDTO>> save(
            @Valid @RequestBody PlayerMatchDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PlayerDTO playerDTO = playerService.getById(dto.getPlayer().getId());
        MatchDTO matchDTO = matchService.getMatchById(dto.getMatch().getId());

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                playerDTO.getTeamId()
        );

        teamAccessValidator.validateAnyTeamOrAdmin(
                principal,
                matchDTO.getHomeTeam() != null ? matchDTO.getHomeTeam().getId() : null,
                matchDTO.getAwayTeam() != null ? matchDTO.getAwayTeam().getId() : null
        );

        log.info("Assigning player {} to match {}", dto.getPlayer().getId(), dto.getMatch().getId());
        PlayerMatchDTO saved = playerMatchService.save(dto);

        return ResponseEntity.ok(ApiResponse.ok("Player match assigned successfully", saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerMatchDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to fetch player match with ID: {}", id);

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
    public ResponseEntity<ApiResponse<PlayerMatchDTO>> update(
            @Valid @RequestBody PlayerMatchDTO playerMatchDTO,
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

        log.info("Request to update playerMatch, ID: {}", playerMatchDTO.getId());
        PlayerMatchDTO playerMatchDTO1 = playerMatchService.updatePlayerMatch(playerMatchDTO);

        return ResponseEntity.ok(ApiResponse.ok("Player match updated successfully!!!", playerMatchDTO1));
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

        log.debug("Request received to delete player match with ID: {}", id);
        playerMatchService.delete(id);

        return ResponseEntity.noContent().build();
    }

}

