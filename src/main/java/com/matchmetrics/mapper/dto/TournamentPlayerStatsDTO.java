package com.matchmetrics.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TournamentPlayerStatsDTO {

    // ── Batting ───────────────────────────────────────────────────────────────
    private int ab;
    private int hits;
    private int singles;
    private int doubles;
    private int triples;
    private int homeRuns;
    private int rbi;
    private int walks;
    private int hitByPitch;
    private int strikeouts;
    private int stolenBases;
    private String avg;
    private String obp;
    private String slg;
    private String ops;

    // ── Pitching ─────────────────────────────────────────────────────────────
    private int pitchingAppearances;
    private int wins;
    private int losses;
    private int saves;
    private String ip;
    private int pitchingStrikeouts;
    private int pitchingWalks;
    private int hitsAllowed;
    private int earnedRuns;
    private String era;
    private String whip;
}
