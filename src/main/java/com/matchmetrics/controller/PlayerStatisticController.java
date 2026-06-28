package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.PlayerMatchDTO;
import com.matchmetrics.mapper.dto.PlayerStatisticDTO;
import com.matchmetrics.service.IPlayerStatisticService;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.security.TeamAccessValidator;
import com.matchmetrics.service.IPlayerMatchService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.matchmetrics.mapper.dto.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/player-statistics")
@Slf4j
public class PlayerStatisticController {
    private final IPlayerStatisticService playerStatisticService;
    private final IPlayerMatchService playerMatchService;
    private final TeamAccessValidator teamAccessValidator;
    public PlayerStatisticController(IPlayerStatisticService playerStatisticService, IPlayerMatchService playerMatchService, TeamAccessValidator teamAccessValidator) {
        this.playerStatisticService = playerStatisticService;
        this.playerMatchService = playerMatchService;
        this.teamAccessValidator = teamAccessValidator;
    }

    /**
     * @deprecated Sistema A — estadísticas manuales por Statistic.
     * Usar /api/v1/player-stats (Sistema B, derivado de BaseballPlayEvent).
     */
    @Deprecated
    @GetMapping
    public ResponseEntity<List<PlayerStatisticDTO>> getAll(
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get all players statistics with search: {}", search);

        if (principal == null || principal.getRole() == UserRole.ADMIN) {
            return ResponseEntity.ok(playerStatisticService.search(search));
        }

        if (principal.getTeamId() == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                playerStatisticService.searchByTeam(search, principal.getTeamId())
        );
    }

    /** @deprecated Sistema A. Usar /api/v1/baseball/play-events o /api/v1/softball/play-events. */
    @Deprecated
    @PostMapping
    public ResponseEntity<ApiResponse<PlayerStatisticDTO>> save(
            @Valid @RequestBody PlayerStatisticDTO playerStatisticDTO,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PlayerMatchDTO playerMatchDTO = playerMatchService.getById(
                playerStatisticDTO.getPlayerMatch().getId()
        );

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                playerMatchDTO.getPlayer() != null ? playerMatchDTO.getPlayer().getTeamId() : null
        );

        log.info("Assigning statistics to players with ID: {}", playerStatisticDTO.getId());
        PlayerStatisticDTO createdStat = playerStatisticService.createPlayerStatistic(playerStatisticDTO);

        return ResponseEntity.ok(ApiResponse.ok("Statistic assigned successfully!!!", createdStat));
    }

    /** @deprecated Sistema A. */
    @Deprecated
    @GetMapping("/{id}")
    public ResponseEntity<PlayerStatisticDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get player statistic with ID: {}", id);

        PlayerStatisticDTO playerStatisticDTO = playerStatisticService.getById(id);

        Long resourceTeamId =
                playerStatisticDTO.getPlayerMatch() != null
                        && playerStatisticDTO.getPlayerMatch().getPlayer() != null
                        ? playerStatisticDTO.getPlayerMatch().getPlayer().getTeamId()
                        : null;

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                resourceTeamId
        );

        return ResponseEntity.ok(playerStatisticDTO);
    }

    /** @deprecated Sistema A. */
    @Deprecated
    @PutMapping
    public ResponseEntity<PlayerStatisticDTO> update(
            @Valid @RequestBody PlayerStatisticDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PlayerStatisticDTO currentStatistic = playerStatisticService.getById(dto.getId());

        Long currentTeamId =
                currentStatistic.getPlayerMatch() != null
                        && currentStatistic.getPlayerMatch().getPlayer() != null
                        ? currentStatistic.getPlayerMatch().getPlayer().getTeamId()
                        : null;

        teamAccessValidator.validateSameTeamOrAdmin(principal, currentTeamId);

        PlayerMatchDTO newPlayerMatch = playerMatchService.getById(
                dto.getPlayerMatch().getId()
        );

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                newPlayerMatch.getPlayer() != null ? newPlayerMatch.getPlayer().getTeamId() : null
        );

        log.info("Request to update statistics for player match ID: {}", dto.getId());
        PlayerStatisticDTO updated = playerStatisticService.update(dto);

        return ResponseEntity.ok(updated);
    }

    /** @deprecated Sistema A. */
    @Deprecated
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PlayerStatisticDTO playerStatisticDTO = playerStatisticService.getById(id);

        Long resourceTeamId =
                playerStatisticDTO.getPlayerMatch() != null
                        && playerStatisticDTO.getPlayerMatch().getPlayer() != null
                        ? playerStatisticDTO.getPlayerMatch().getPlayer().getTeamId()
                        : null;

        teamAccessValidator.validateSameTeamOrAdmin(
                principal,
                resourceTeamId
        );

        log.debug("Request received to delete player statistic with ID: {}", id);
        playerStatisticService.delete(id);

        return ResponseEntity.noContent().build();
    }
}

