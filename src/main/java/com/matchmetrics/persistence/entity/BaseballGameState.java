package com.matchmetrics.persistence.entity;

import com.matchmetrics.domain.enums.BaseballGameStatus;
import com.matchmetrics.domain.enums.InningHalf;
import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "baseball_game_state")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseballGameState extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @Column(name = "current_inning", nullable = false)
    private Integer currentInning = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "inning_half", nullable = false)
    private InningHalf inningHalf = InningHalf.TOP;

    @Column(name = "outs", nullable = false)
    private Integer outs = 0;

    @Column(name = "balls", nullable = false)
    private Integer balls = 0;

    @Column(name = "strikes", nullable = false)
    private Integer strikes = 0;

    @Column(name = "home_score", nullable = false)
    private Integer homeScore = 0;

    @Column(name = "away_score", nullable = false)
    private Integer awayScore = 0;

    @ManyToOne
    @JoinColumn(name = "first_base_player_match_id")
    private PlayerMatch firstBasePlayerMatch;

    @ManyToOne
    @JoinColumn(name = "second_base_player_match_id")
    private PlayerMatch secondBasePlayerMatch;

    @ManyToOne
    @JoinColumn(name = "third_base_player_match_id")
    private PlayerMatch thirdBasePlayerMatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BaseballGameStatus status = BaseballGameStatus.NOT_STARTED;
}
