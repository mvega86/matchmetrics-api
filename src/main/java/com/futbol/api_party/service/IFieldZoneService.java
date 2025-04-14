package com.futbol.api_party.service;

import com.futbol.api_party.mapper.dto.FieldZoneDTO;

import java.util.Optional;

public interface IFieldZoneService {
    Optional<FieldZoneDTO> getZoneByPosition(Double x, Double y);
}
