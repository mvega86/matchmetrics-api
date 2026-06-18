package com.matchmetrics.service.implementation;

import com.matchmetrics.mapper.dto.StatisticLocationDTO;
import com.matchmetrics.persistence.repository.StatisticLocationRepository;
import com.matchmetrics.service.IStatisticLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticLocationService implements IStatisticLocationService {

    private final StatisticLocationRepository statisticLocationRepository;

    @Override
    public List<StatisticLocationDTO> getAll() {
        log.info("Retrieving all statistic locations...");
        return statisticLocationRepository.findAll().stream()
                .map(loc -> {
                    StatisticLocationDTO dto = new StatisticLocationDTO();
                    dto.setId(loc.getId());
                    dto.setName(loc.getName());
                    return dto;
                })
                .toList();
    }
}
