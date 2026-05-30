package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.FieldZoneDTO;

import java.util.Optional;

public interface IFieldZoneService {
    Optional<FieldZoneDTO> getZoneByPosition(Double x, Double y);
}
