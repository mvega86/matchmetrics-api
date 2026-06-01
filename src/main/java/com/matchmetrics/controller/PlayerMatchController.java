package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.PlayerMatchDTO;
import com.matchmetrics.service.IPlayerMatchService;
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
@RequestMapping("/api/v1/players-match")
@Slf4j
public class PlayerMatchController {

    private final IPlayerMatchService playerMatchService;

    public PlayerMatchController(IPlayerMatchService playerMatchService) {
        this.playerMatchService = playerMatchService;
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
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody PlayerMatchDTO dto) {
        log.info("Logger: Assigning player {} to match {}", dto.getPlayer().getId(), dto.getMatch().getId());
        PlayerMatchDTO saved = playerMatchService.save(dto);
        log.info("Logger: Player match assigned successfully");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Player match assigned successfully");
        response.put("data", saved);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PlayerMatchDTO> getById(@PathVariable Long id) {
        log.info("Logger: Request to fetch players for match ID: {}", id);
        return ResponseEntity.ok(playerMatchService.getById(id));
    }

    @PutMapping()
    public ResponseEntity<Map<String, Object>> update(@RequestBody PlayerMatchDTO playerMatchDTO) {
        log.info("Logger: Request to update playerMatch, ID: {}", playerMatchDTO.getId());
        PlayerMatchDTO playerMatchDTO1 = playerMatchService.updatePlayerMatch(playerMatchDTO);
        log.info("Logger: Player match updated.");
        return ResponseEntity.ok(Map.of(
                "message", "Player match updated successfully!!!",
                "data", playerMatchDTO1
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Logger: Request received to delete player match with ID: {}", id);
        playerMatchService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

