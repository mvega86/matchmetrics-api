package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.PlayerStatisticDTO;
import com.matchmetrics.service.IPlayerStatisticService;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/player-statistics")
@Slf4j
public class PlayerStatisticController {
    private final IPlayerStatisticService playerStatisticService;
    public PlayerStatisticController(IPlayerStatisticService playerStatisticService) {
        this.playerStatisticService = playerStatisticService;
    }
    @GetMapping
    public ResponseEntity<List<PlayerStatisticDTO>> getAll(
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Logger: Request to get all players statistics with search: {}", search);

        if (principal.getRole() == UserRole.ADMIN) {
            return ResponseEntity.ok(playerStatisticService.search(search));
        }

        if (principal.getTeamId() == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                playerStatisticService.searchByTeam(search, principal.getTeamId())
        );
    }
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPlayerStatistics(@Valid @RequestBody PlayerStatisticDTO playerStatisticDTO) {
        log.info("Assigning statistics to players with ID: {}", playerStatisticDTO.getId());
        PlayerStatisticDTO createdStat = playerStatisticService.createPlayerStatistic(playerStatisticDTO);
        log.info("Statistic player assigned successfully!!!");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Statistic assigned successfully!!!");
        response.put("data", createdStat);

        return ResponseEntity.ok(response);
    }
    @PutMapping
    public ResponseEntity<PlayerStatisticDTO> updateStatistic(@RequestBody PlayerStatisticDTO dto) {
        log.info("Request to fetch statistics for player match ID: {}", dto.getId());
        PlayerStatisticDTO updated = playerStatisticService.update(dto);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Request received to delete player statistic with ID: {}", id);
        playerStatisticService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

