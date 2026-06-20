package com.matchmetrics.persistence.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.matchmetrics.domain.enums.MatchPhase;
import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Match extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false)
    private SportType sportType = SportType.FOOTBALL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchState state = MatchState.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchPhase phase = MatchPhase.NOT_STARTED;

    @ManyToOne
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(name = "home_score", nullable = false)
    private int homeScore = 0;

    @Column(name = "away_score", nullable = false)
    private int awayScore = 0;

    private LocalDateTime startFirstTime;
    private LocalDateTime endFirstTime;
    private LocalDateTime startSecondTime;
    private LocalDateTime endSecondTime;
    private LocalDateTime startFirstExtraTime;
    private LocalDateTime endFirstExtraTime;
    private LocalDateTime startSecondExtraTime;
    private LocalDateTime endSecondExtraTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    @JsonBackReference
    private Tournament tournament;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PlayerMatch> playerMatches;
}

