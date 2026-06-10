package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.BaseballEventType;
import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.mapper.dto.TournamentPlayerStatsDTO;
import com.matchmetrics.persistence.entity.BaseballPlayEvent;
import com.matchmetrics.persistence.entity.Match;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.repository.BaseballPlayEventRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import com.matchmetrics.service.ISoftballStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SoftballStatsService implements ISoftballStatsService {

    private final PlayerMatchRepository playerMatchRepository;
    private final BaseballPlayEventRepository playEventRepository;

    private static final Set<BaseballEventType> AT_BAT_TYPES = Set.of(
            BaseballEventType.SINGLE, BaseballEventType.DOUBLE, BaseballEventType.TRIPLE,
            BaseballEventType.HOME_RUN, BaseballEventType.STRIKEOUT, BaseballEventType.OUT,
            BaseballEventType.DOUBLE_PLAY, BaseballEventType.TRIPLE_PLAY, BaseballEventType.ERROR
    );

    private static final Set<BaseballEventType> HIT_TYPES = Set.of(
            BaseballEventType.SINGLE, BaseballEventType.DOUBLE,
            BaseballEventType.TRIPLE, BaseballEventType.HOME_RUN
    );

    @Override
    public TournamentPlayerStatsDTO getPlayerTournamentStats(Long playerMatchId) {
        PlayerMatch pm = playerMatchRepository.findById(playerMatchId)
                .orElseThrow(() -> new IllegalArgumentException("PlayerMatch not found: " + playerMatchId));

        Long playerId = pm.getPlayer().getId();
        Long tournamentId = pm.getMatch().getTournament() != null
                ? pm.getMatch().getTournament().getId()
                : null;

        if (tournamentId == null) {
            return emptyStats();
        }

        List<BaseballPlayEvent> battingEvents =
                playEventRepository.findBattingEventsByTournamentAndPlayer(tournamentId, playerId);
        List<BaseballPlayEvent> pitchingEvents =
                playEventRepository.findPitchingEventsByTournamentAndPlayer(tournamentId, playerId);

        return buildStats(battingEvents, pitchingEvents);
    }

    private TournamentPlayerStatsDTO buildStats(
            List<BaseballPlayEvent> battingEvents,
            List<BaseballPlayEvent> pitchingEvents
    ) {
        TournamentPlayerStatsDTO dto = new TournamentPlayerStatsDTO();

        // ── Batting ──────────────────────────────────────────────────────────
        int singles  = count(battingEvents, BaseballEventType.SINGLE);
        int doubles  = count(battingEvents, BaseballEventType.DOUBLE);
        int triples  = count(battingEvents, BaseballEventType.TRIPLE);
        int homeRuns = count(battingEvents, BaseballEventType.HOME_RUN);
        int hits     = singles + doubles + triples + homeRuns;
        int bb       = count(battingEvents, BaseballEventType.WALK);
        int hbp      = count(battingEvents, BaseballEventType.HIT_BY_PITCH);
        int sacFly   = count(battingEvents, BaseballEventType.SACRIFICE_FLY);
        int strikeouts = count(battingEvents, BaseballEventType.STRIKEOUT);
        int rbi      = battingEvents.stream().mapToInt(e -> e.getRbi() != null ? e.getRbi() : 0).sum();

        int ab = (int) battingEvents.stream()
                .filter(e -> AT_BAT_TYPES.contains(e.getEventType()))
                .count();

        dto.setAb(ab);
        dto.setHits(hits);
        dto.setSingles(singles);
        dto.setDoubles(doubles);
        dto.setTriples(triples);
        dto.setHomeRuns(homeRuns);
        dto.setRbi(rbi);
        dto.setWalks(bb);
        dto.setHitByPitch(hbp);
        dto.setStrikeouts(strikeouts);
        dto.setAvg(formatAvg(ab > 0 ? (double) hits / ab : 0.0));
        dto.setObp(formatAvg(obp(hits, bb, hbp, ab, sacFly)));
        dto.setSlg(formatAvg(ab > 0 ? (double) totalBases(singles, doubles, triples, homeRuns) / ab : 0.0));

        // ── Pitching ─────────────────────────────────────────────────────────
        // outsOnPlay may be 0 even for real outs — derive from event type as fallback
        int totalOuts = pitchingEvents.stream().mapToInt(this::outsFromEvent).sum();
        double ipDecimal = totalOuts / 3.0;

        int pitchingK  = count(pitchingEvents, BaseballEventType.STRIKEOUT);
        int pitchingBB = count(pitchingEvents, BaseballEventType.WALK);
        int hitsAllowed = (int) pitchingEvents.stream()
                .filter(e -> HIT_TYPES.contains(e.getEventType()))
                .count();
        int earnedRuns = pitchingEvents.stream().mapToInt(e -> e.getRunsScored() != null ? e.getRunsScored() : 0).sum();

        int appearances = (int) pitchingEvents.stream()
                .map(e -> e.getMatch().getId())
                .distinct()
                .count();

        int[] wl = computeWinsLosses(pitchingEvents);

        dto.setPitchingAppearances(appearances);
        dto.setWins(wl[0]);
        dto.setLosses(wl[1]);
        dto.setIp(formatIp(totalOuts));
        dto.setPitchingStrikeouts(pitchingK);
        dto.setPitchingWalks(pitchingBB);
        dto.setHitsAllowed(hitsAllowed);
        dto.setEarnedRuns(earnedRuns);
        dto.setEra(ipDecimal > 0 ? formatEra(earnedRuns * 9.0 / ipDecimal) : "0.00");
        dto.setWhip(ipDecimal > 0 ? formatEra((hitsAllowed + pitchingBB) / ipDecimal) : "0.00");

        return dto;
    }

    private int[] computeWinsLosses(List<BaseballPlayEvent> pitchingEvents) {
        Map<Long, List<BaseballPlayEvent>> byMatch = pitchingEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getMatch().getId()));

        int wins = 0, losses = 0;
        for (List<BaseballPlayEvent> matchEvents : byMatch.values()) {
            BaseballPlayEvent sample = matchEvents.get(0);
            Match match = sample.getMatch();
            if (match.getState() != MatchState.FINISHED) continue;
            Long pitcherTeamId = sample.getPitcherPlayerMatch().getPlayer().getTeam().getId();
            boolean isHome = match.getHomeTeam().getId().equals(pitcherTeamId);
            int teamScore = isHome ? match.getHomeScore() : match.getAwayScore();
            int oppScore  = isHome ? match.getAwayScore()  : match.getHomeScore();
            if (teamScore > oppScore) wins++;
            else if (oppScore > teamScore) losses++;
        }
        return new int[]{wins, losses};
    }

    private int outsFromEvent(BaseballPlayEvent e) {
        int recorded = e.getOutsOnPlay() != null ? e.getOutsOnPlay() : 0;
        if (recorded > 0) return recorded;
        return switch (e.getEventType()) {
            case STRIKEOUT, OUT, CAUGHT_STEALING -> 1;
            case DOUBLE_PLAY -> 2;
            case TRIPLE_PLAY -> 3;
            default -> 0;
        };
    }

    private int count(List<BaseballPlayEvent> events, BaseballEventType type) {
        return (int) events.stream().filter(e -> e.getEventType() == type).count();
    }

    private int totalBases(int s, int d, int t, int hr) {
        return s + 2 * d + 3 * t + 4 * hr;
    }

    private double obp(int h, int bb, int hbp, int ab, int sf) {
        int denom = ab + bb + hbp + sf;
        return denom > 0 ? (double) (h + bb + hbp) / denom : 0.0;
    }

    private String formatAvg(double val) {
        if (val >= 1.0) return String.format("%.3f", val);
        String s = String.format("%.3f", val);
        // Remove leading zero: "0.333" → ".333"
        return s.startsWith("0.") ? s.substring(1) : s;
    }

    private String formatEra(double val) {
        return String.format("%.2f", val);
    }

    private String formatIp(int outs) {
        return (outs / 3) + "." + (outs % 3);
    }

    private TournamentPlayerStatsDTO emptyStats() {
        TournamentPlayerStatsDTO dto = new TournamentPlayerStatsDTO();
        dto.setAvg(".000"); dto.setObp(".000"); dto.setSlg(".000");
        dto.setIp("0.0"); dto.setEra("0.00"); dto.setWhip("0.00");
        return dto;
    }
}
