package com.matchmetrics.mapper;

import com.matchmetrics.mapper.dto.StatisticDTO;
import com.matchmetrics.persistence.entity.Statistic;
import org.springframework.stereotype.Component;

@Component
public class StatisticMapper {

    public StatisticDTO toDTO(Statistic statistic) {
        StatisticDTO dto = new StatisticDTO();
        dto.setId(statistic.getId());
        dto.setName(statistic.getName());
        dto.setDescription(statistic.getDescription());
        dto.setUnit(statistic.getUnit());
        return dto;
    }
}

