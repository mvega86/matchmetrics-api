package com.futbol.api_party.service.implementation;

import com.futbol.api_party.mapper.dto.FieldZoneDTO;
import com.futbol.api_party.persistence.entity.FieldZone;
import com.futbol.api_party.persistence.repository.FieldZoneRepository;
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
