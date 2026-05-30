package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.StatisticDTO;
import com.matchmetrics.service.IStatisticService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticController {

    private final IStatisticService statisticService;

    public StatisticController(IStatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createStatistic(@Valid @RequestBody StatisticDTO statisticDTO) {
        log.info("Request received to save statistic: {}", statisticDTO.getName());
        StatisticDTO saved = statisticService.createStatistic(statisticDTO);
        log.info("Successfully saved statistic!!!");
        return ResponseEntity.ok(Map.of(
                "message", "Successfully saved statistic!!!",
                "data", saved
        ));
    }

    @GetMapping
    public List<StatisticDTO> getAllStatistics() {
        log.info("Request to get all statistic...");
        return statisticService.getAllStatistics();
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateStatistic(@Valid @RequestBody StatisticDTO statisticDTO) {
        log.info("Request to update statistic...");
        StatisticDTO statisticDTOOut = statisticService.updateStatistic(statisticDTO);
        log.info("Successfully updated statistic!!!");
        return ResponseEntity.ok(Map.of(
                "message", "Successfully updated statistic!!!",
                "data", statisticDTOOut
        ));
    }

    @DeleteMapping("/{id}")
    public void deleteStatistic(@PathVariable Long id) {
        log.info("Request to delete statistic...");
        statisticService.deleteStatistic(id);
        log.info("Statistic deleted.");
    }
}

