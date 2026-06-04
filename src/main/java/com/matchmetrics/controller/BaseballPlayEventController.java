package com.matchmetrics.controller;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.mapper.dto.BaseballPlayEventDTO;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IBaseballPlayEventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/baseball/play-events")
@CrossOrigin(origins = "http://localhost:5173")
public class BaseballPlayEventController {

    private final IBaseballPlayEventService playEventService;

    public BaseballPlayEventController(IBaseballPlayEventService playEventService) {
        this.playEventService = playEventService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody BaseballPlayEventDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to create play event for match: {}", dto.getMatchId());

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            log.warn("User {} tried to create play event without permission", principal.getEmail());
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballPlayEventDTO created = playEventService.createPlayEvent(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Play event created successfully",
                    "data", created
            ));
        } catch (Exception e) {
            log.error("Error creating play event: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseballPlayEventDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to get play event: {}", id);
        try {
            return ResponseEntity.ok(playEventService.getPlayEventById(id));
        } catch (Exception e) {
            log.error("Play event not found: {}", id);
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<BaseballPlayEventDTO>> getAll(
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to list play events with search: {}", search);
        return ResponseEntity.ok(playEventService.search(search));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody BaseballPlayEventDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to update play event: {}", id);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            log.warn("User {} tried to update play event without permission", principal.getEmail());
            return ResponseEntity.status(403).build();
        }

        try {
            BaseballPlayEventDTO updated = playEventService.updatePlayEvent(id, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Play event updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating play event {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Request to delete play event: {}", id);

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.MANAGER) {
            log.warn("User {} tried to delete play event without permission", principal.getEmail());
            return ResponseEntity.status(403).build();
        }

        try {
            playEventService.deletePlayEvent(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting play event {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }
}
