package com.futbol.api_party.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field_zone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(name = "min_x")
    private Double minX;

    @Column(name = "max_x")
    private Double maxX;

    @Column(name = "min_y")
    private Double minY;

    @Column(name = "max_y")
    private Double maxY;
}
