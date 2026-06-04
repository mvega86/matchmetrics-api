package com.matchmetrics.mapper;

import com.matchmetrics.domain.enums.SportType;
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
        dto.setSportType(statistic.getSportType());
        return dto;
    }

    public Statistic toEntity(StatisticDTO dto) {
        Statistic statistic = new Statistic();
        statistic.setName(dto.getName());
        statistic.setDescription(dto.getDescription());
        statistic.setUnit(dto.getUnit());
        statistic.setSportType(dto.getSportType() != null ? dto.getSportType() : SportType.FOOTBALL);
        return statistic;
    }
}

