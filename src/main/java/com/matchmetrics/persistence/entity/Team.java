package com.matchmetrics.persistence.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

// =========================
// ENTIDAD Team
// =========================
@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "acronym", nullable = false)
    private String acronym;

    @Column(name = "stadium", nullable = false)
    private String stadium;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false)
    private SportType sportType = SportType.FOOTBALL;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Player> players;
}
