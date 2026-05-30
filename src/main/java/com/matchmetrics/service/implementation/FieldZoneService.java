package com.matchmetrics.service.implementation;

import com.matchmetrics.mapper.dto.FieldZoneDTO;
import com.matchmetrics.persistence.entity.FieldZone;
import com.matchmetrics.persistence.repository.FieldZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FieldZoneService {

    private final FieldZoneRepository fieldZoneRepository;

    public Optional<FieldZoneDTO> getZoneByPosition(Double x, Double y) {
        FieldZone zone = fieldZoneRepository.findByPosition(x, y);
        if (zone == null) return Optional.empty();

        return Optional.of(FieldZoneDTO.builder()
                .id(zone.getId())
                .name(zone.getName())
                .description(zone.getDescription())
                .build());
    }
}
