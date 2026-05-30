// StatisticWayController.java
package com.matchmetrics.controller;

import com.matchmetrics.persistence.entity.StatisticWay;
import com.matchmetrics.persistence.repository.StatisticWayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/statistic-ways")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class StatisticWayController {

    private final StatisticWayRepository repository;

    @GetMapping
    public List<StatisticWay> getAll() {
        log.info("Request received to retrieve all statistic ways...");
        return repository.findAll();
    }
}
