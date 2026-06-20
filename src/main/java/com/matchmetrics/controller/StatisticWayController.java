// StatisticWayController.java
package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.StatisticWayDTO;
import com.matchmetrics.service.IStatisticWayService;
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

    private final IStatisticWayService statisticWayService;

    @GetMapping
    public List<StatisticWayDTO> getAll() {
        log.info("Request received to retrieve all statistic ways...");
        return statisticWayService.getAll();
    }
}
