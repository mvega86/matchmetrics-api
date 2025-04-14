package com.futbol.api_party.mapper.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldZoneDTO {
    private Long id;
    private String name;
    private String description;
}
