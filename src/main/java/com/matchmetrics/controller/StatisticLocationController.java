// StatisticLocationController.java
package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.StatisticLocationDTO;
import com.matchmetrics.service.IStatisticLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/statistic-locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class StatisticLocationController {

    private final IStatisticLocationService statisticLocationService;

    @GetMapping
    public List<StatisticLocationDTO> getAll() {
        log.info("Request received to retrieve all statistic locations...");
        return statisticLocationService.getAll();
    }
}
