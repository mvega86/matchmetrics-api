package com.matchmetrics.persistence.entity;

import com.matchmetrics.domain.enums.BaseballEventType;
import com.matchmetrics.domain.enums.InningHalf;
import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "baseball_play_event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseballPlayEvent extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private Integer inning;

    @Enumerated(EnumType.STRING)
    @Column(name = "inning_half", nullable = false)
    private InningHalf inningHalf;

    @ManyToOne
    @JoinColumn(name = "batting_team_id", nullable = false)
    private Team battingTeam;

    @ManyToOne
    @JoinColumn(name = "fielding_team_id", nullable = false)
    private Team fieldingTeam;

    @ManyToOne
    @JoinColumn(name = "batter_player_match_id")
    private PlayerMatch batterPlayerMatch;

    @ManyToOne
    @JoinColumn(name = "pitcher_player_match_id")
    private PlayerMatch pitcherPlayerMatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private BaseballEventType eventType;

    @Column(length = 255)
    private String result;

    @Column(name = "runs_scored")
    private Integer runsScored = 0;

    @Column(name = "outs_on_play")
    private Integer outsOnPlay = 0;

    @Column(name = "rbi")
    private Integer rbi = 0;

    @Column(length = 500)
    private String description;

    @Column(name = "balls_before")
    private Integer ballsBefore = 0;

    @Column(name = "strikes_before")
    private Integer strikesBefore = 0;

    @Column(name = "outs_before")
    private Integer outsBefore = 0;

    @Column(name = "first_base_before")
    private Boolean firstBaseBefore = false;

    @Column(name = "second_base_before")
    private Boolean secondBaseBefore = false;

    @Column(name = "third_base_before")
    private Boolean thirdBaseBefore = false;
}
