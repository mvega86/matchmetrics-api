package com.matchmetrics.persistence.entity;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "statistic",
    uniqueConstraints = @UniqueConstraint(columnNames = {"name", "sport_type"})
)
public class Statistic extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 10)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false)
    private SportType sportType = SportType.FOOTBALL;
}

