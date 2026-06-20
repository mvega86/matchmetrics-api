package com.matchmetrics.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "pitcher_pitch_count",
    uniqueConstraints = @UniqueConstraint(columnNames = {"game_state_id", "pitcher_player_match_id"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PitcherPitchCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_state_id", nullable = false)
    private BaseballGameState gameState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pitcher_player_match_id", nullable = false)
    private PlayerMatch pitcherPlayerMatch;

    @Column(name = "pitch_count", nullable = false)
    private Integer pitchCount = 0;
}
