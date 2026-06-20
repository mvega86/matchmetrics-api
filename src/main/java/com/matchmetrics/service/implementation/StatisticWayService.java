package com.matchmetrics.service.implementation;

import com.matchmetrics.mapper.dto.StatisticWayDTO;
import com.matchmetrics.persistence.repository.StatisticWayRepository;
import com.matchmetrics.service.IStatisticWayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticWayService implements IStatisticWayService {

    private final StatisticWayRepository statisticWayRepository;

    @Override
    public List<StatisticWayDTO> getAll() {
        log.info("Retrieving all statistic ways...");
        return statisticWayRepository.findAll().stream()
                .map(way -> {
                    StatisticWayDTO dto = new StatisticWayDTO();
                    dto.setId(way.getId());
                    dto.setName(way.getName());
                    return dto;
                })
                .toList();
    }
}
