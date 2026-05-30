package com.matchmetrics.mapper;

import com.matchmetrics.mapper.dto.FieldZoneDTO;
import com.matchmetrics.persistence.entity.FieldZone;
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


