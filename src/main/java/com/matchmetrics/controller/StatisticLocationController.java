// StatisticLocationController.java
package com.matchmetrics.controller;

import com.matchmetrics.persistence.entity.StatisticLocation;
import com.matchmetrics.persistence.repository.StatisticLocationRepository;
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

    private final StatisticLocationRepository repository;

    @GetMapping
    public List<StatisticLocation> getAll() {
        log.info("Request received to retrieve all statistic locations...");
        return repository.findAll();
    }
}
