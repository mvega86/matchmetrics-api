package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.FieldZoneDTO;
import com.matchmetrics.service.implementation.FieldZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
public class FieldZoneController {

    private final FieldZoneService fieldZoneService;

    @GetMapping("/search")
    public ResponseEntity<FieldZoneDTO> getByPosition(
            @RequestParam Double x,
            @RequestParam Double y
    ) {
        return fieldZoneService.getZoneByPosition(x, y)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

