package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.StatisticDTO;
import com.matchmetrics.service.IStatisticService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.matchmetrics.mapper.dto.ApiResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticController {

    private final IStatisticService statisticService;

    public StatisticController(IStatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StatisticDTO>> createStatistic(@Valid @RequestBody StatisticDTO statisticDTO) {
        log.info("Request received to save statistic: {}", statisticDTO.getName());
        StatisticDTO saved = statisticService.createStatistic(statisticDTO);
        log.info("Successfully saved statistic!!!");
        return ResponseEntity.ok(ApiResponse.ok("Successfully saved statistic!!!", saved));
    }

    @GetMapping
    public List<StatisticDTO> getAllStatistics(
            @RequestParam(value = "search", required = false) String search
    ) {
        log.info("Request to get statistics with search: {}", search);
        return statisticService.search(search);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<StatisticDTO>> updateStatistic(@Valid @RequestBody StatisticDTO statisticDTO) {
        log.info("Request to update statistic...");
        StatisticDTO statisticDTOOut = statisticService.updateStatistic(statisticDTO);
        log.info("Successfully updated statistic!!!");
        return ResponseEntity.ok(ApiResponse.ok("Successfully updated statistic!!!", statisticDTOOut));
    }

    @DeleteMapping("/{id}")
    public void deleteStatistic(@PathVariable Long id) {
        log.info("Request to delete statistic...");
        statisticService.deleteStatistic(id);
        log.info("Statistic deleted.");
    }
}

