package com.futbol.api_party.persistence.entity;

import com.futbol.api_party.persistence.audit.AuditModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PlayerStatistic extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_match_id", nullable = false)
    private PlayerMatch playerMatch;

    @ManyToOne
    @JoinColumn(name = "statistic_id", nullable = false)
    private Statistic statistic;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "position_x")
    private Double positionX;
    @Column(name = "position_y")
    private Double positionY;

    @Column(length = 255)
    private String observation;
}

