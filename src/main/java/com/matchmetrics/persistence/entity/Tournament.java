package com.matchmetrics.persistence.entity;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.domain.enums.TournamentStatus;
import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournament")
@Getter
@Setter
@NoArgsConstructor
public class Tournament extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false)
    private SportType sportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status = TournamentStatus.UPCOMING;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private String organizer;

    private String location;

    private String country;

    private String category;

    @ManyToMany
    @JoinTable(
        name = "tournament_team",
        joinColumns = @JoinColumn(name = "tournament_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "tournament")
    private List<Match> matches = new ArrayList<>();
}
