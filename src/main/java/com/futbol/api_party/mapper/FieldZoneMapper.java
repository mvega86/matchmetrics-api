package com.futbol.api_party.mapper;

import com.futbol.api_party.mapper.dto.FieldZoneDTO;
import com.futbol.api_party.mapper.dto.PlayerStatisticDTO;
import com.futbol.api_party.persistence.entity.FieldZone;
import com.futbol.api_party.persistence.entity.PlayerMatch;
import com.futbol.api_party.persistence.entity.PlayerStatistic;
import com.futbol.api_party.persistence.entity.Statistic;
import com.futbol.api_party.utils.MatchTimeUtil;
import org.springframework.stereotype.Component;
@Component
public class FieldZoneMapper {
    public FieldZone toEntity(FieldZoneDTO dto) {
        FieldZone fieldZone = new FieldZone();
        fieldZone.setId(dto.getId());
        fieldZone.setName(dto.getName());
        fieldZone.setDescription(dto.getDescription());
        return fieldZone;
    }
    public FieldZoneDTO toDTO(FieldZone fieldZone) {
        FieldZoneDTO dto = new FieldZoneDTO();
        dto.setId(fieldZone.getId());
        dto.setName(fieldZone.getName());
        dto.setDescription(fieldZone.getDescription());

        return dto;
    }
}


