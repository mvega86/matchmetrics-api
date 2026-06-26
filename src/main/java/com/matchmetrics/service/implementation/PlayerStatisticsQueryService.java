package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.BaseballEventType;
import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.dto.PlayerStatisticsDetailDTO;
import com.matchmetrics.mapper.dto.PlayerStatisticsDetailDTO.TournamentBreakdownDTO;
import com.matchmetrics.mapper.dto.PlayerStatisticsSummaryDTO;
import com.matchmetrics.mapper.dto.TournamentPlayerStatsDTO;
import com.matchmetrics.persistence.entity.BaseballPlayEvent;
import com.matchmetrics.persistence.entity.Player;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.persistence.repository.BaseballPlayEventRepository;
import com.matchmetrics.persistence.repository.PlayerRepository;
import com.matchmetrics.service.IPlayerStatisticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerStatisticsQueryService implements IPlayerStatisticsQueryService {

    private final BaseballPlayEventRepository eventRepo;
    private final PlayerRepository playerRepo;

    private static final Set<BaseballEventType> AT_BAT_TYPES = Set.of(
            BaseballEventType.SINGLE, BaseballEventType.DOUBLE, BaseballEventType.TRIPLE,
            BaseballEventType.HOME_RUN, BaseballEventType.STRIKEOUT, BaseballEventType.OUT,
            BaseballEventType.DOUBLE_PLAY, BaseballEventType.TRIPLE_PLAY, BaseballEventType.ERROR
    );

    private static final Set<BaseballEventType> HIT_TYPES = Set.of(
            BaseballEventType.SINGLE, BaseballEventType.DOUBLE,
            BaseballEventType.TRIPLE, BaseballEventType.HOME_RUN
    );

    // ── Public API ────────────────────────────────────────────────────────────

    @Override
    public List<PlayerStatisticsSummaryDTO> getPlayerStatsList(SportType sportType, Long teamId, Long tournamentId) {
        // Filter by team at SQL level to avoid loading all events into memory.
        List<BaseballPlayEvent> battingEvents;
        if (teamId != null) {
            battingEvents = tournamentId != null
                    ? eventRepo.findAllBattingEventsBySportTypeAndTournamentAndTeam(sportType, tournamentId, teamId)
                    : eventRepo.findAllBattingEventsBySportTypeAndTeam(sportType, teamId);
        } else {
            battingEvents = tournamentId != null
                    ? eventRepo.findAllBattingEventsBySportTypeAndTournament(sportType, tournamentId)
                    : eventRepo.findAllBattingEventsBySportType(sportType);
        }

        Map<Long, List<BaseballPlayEvent>> byPlayer = battingEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getBatterPlayerMatch().getPlayer().getId()));

        List<Long> playerIds = new ArrayList<>(byPlayer.keySet());
        List<BaseballPlayEvent> pitchingEvents = playerIds.isEmpty()
                ? Collections.emptyList()
                : tournamentId != null
                    ? eventRepo.findAllPitchingEventsBySportTypeAndTournamentAndPlayerIds(sportType, tournamentId, playerIds)
                    : eventRepo.findAllPitchingEventsBySportTypeAndPlayerIds(sportType, playerIds);

        Map<Long, List<BaseballPlayEvent>> pitchingByPlayer = pitchingEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getPitcherPlayerMatch().getPlayer().getId()));

        List<PlayerStatisticsSummaryDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<BaseballPlayEvent>> entry : byPlayer.entrySet()) {
            Long playerId = entry.getKey();
            List<BaseballPlayEvent> events = entry.getValue();
            BaseballPlayEvent sample = events.get(0);
            Player player = sample.getBatterPlayerMatch().getPlayer();
            Team team = player.getTeam();

            int games = (int) events.stream().map(e -> e.getMatch().getId()).distinct().count();
            int singles    = count(events, BaseballEventType.SINGLE);
            int doubles    = count(events, BaseballEventType.DOUBLE);
            int triples    = count(events, BaseballEventType.TRIPLE);
            int homeRuns   = count(events, BaseballEventType.HOME_RUN);
            int hits       = singles + doubles + triples + homeRuns;
            int bb         = count(events, BaseballEventType.WALK);
            int hbp        = count(events, BaseballEventType.HIT_BY_PITCH);
            int sacFly     = count(events, BaseballEventType.SACRIFICE_FLY);
            int strikeouts = count(events, BaseballEventType.STRIKEOUT);
            int rbi        = events.stream().mapToInt(e -> e.getRbi() != null ? e.getRbi() : 0).sum();
            int ab         = (int) events.stream().filter(e -> AT_BAT_TYPES.contains(e.getEventType())).count();

            double avgVal = ab > 0 ? (double) hits / ab : 0.0;
            double obpVal = obp(hits, bb, hbp, ab, sacFly);
            double slgVal = ab > 0 ? (double) totalBases(singles, doubles, triples, homeRuns) / ab : 0.0;
            double opsVal = obpVal + slgVal;

            PlayerStatisticsSummaryDTO dto = new PlayerStatisticsSummaryDTO();
            dto.setPlayerId(player.getId());
            dto.setPlayerName(player.getFullName());
            dto.setJerseyName(player.getJerseyName());
            dto.setJerseyNumber(player.getJerseyNumber());
            if (team != null) {
                dto.setTeamId(team.getId());
                dto.setTeamName(team.getName());
                dto.setTeamAcronym(team.getAcronym());
                dto.setTeamPhotoUrl(team.getPhotoUrl());
            }
            dto.setPlayerPhotoUrl(player.getPhotoUrl());
            dto.setGames(games);
            dto.setAb(ab);
            dto.setHits(hits);
            dto.setSingles(singles);
            dto.setDoubles(doubles);
            dto.setTriples(triples);
            dto.setHomeRuns(homeRuns);
            dto.setRbi(rbi);
            dto.setWalks(bb);
            dto.setStrikeouts(strikeouts);
            dto.setAvg(formatAvg(avgVal));
            dto.setObp(formatAvg(obpVal));
            dto.setSlg(formatAvg(slgVal));
            dto.setOps(formatAvg(opsVal));

            // Pitching
            List<BaseballPlayEvent> pEvents = pitchingByPlayer.getOrDefault(playerId, Collections.emptyList());
            int totalOuts   = pEvents.stream().mapToInt(this::outsFromEvent).sum();
            double ipDec    = totalOuts / 3.0;
            int pitchingK   = count(pEvents, BaseballEventType.STRIKEOUT);
            int pitchingBB  = count(pEvents, BaseballEventType.WALK);
            int hAllowed    = (int) pEvents.stream().filter(e -> HIT_TYPES.contains(e.getEventType())).count();
            int earnedRuns  = pEvents.stream().mapToInt(e -> e.getRunsScored() != null ? e.getRunsScored() : 0).sum();
            int appearances = (int) pEvents.stream().map(e -> e.getMatch().getId()).distinct().count();
            double eraMultiplier = sportType == SportType.SOFTBALL ? 7.0 : 9.0;

            dto.setPitchingAppearances(appearances);
            dto.setIp(formatIp(totalOuts));
            dto.setPitchingStrikeouts(pitchingK);
            dto.setPitchingWalks(pitchingBB);
            dto.setHitsAllowed(hAllowed);
            dto.setEarnedRuns(earnedRuns);
            dto.setEra(ipDec > 0 ? formatEra(earnedRuns * eraMultiplier / ipDec) : "0.00");
            dto.setWhip(ipDec > 0 ? formatEra((hAllowed + pitchingBB) / ipDec) : "0.00");

            result.add(dto);
        }

        result.sort(Comparator.comparing(PlayerStatisticsSummaryDTO::getAvg).reversed());
        return result;
    }

    @Override
    public PlayerStatisticsDetailDTO getPlayerStatsDetail(Long playerId, Long tournamentId) {
        Player player = playerRepo.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + playerId));

        SportType sportType = player.getTeam() != null ? player.getTeam().getSportType() : SportType.SOFTBALL;

        List<BaseballPlayEvent> allBatting  = eventRepo.findBattingEventsByPlayer(playerId);
        List<BaseballPlayEvent> allPitching = eventRepo.findPitchingEventsByPlayer(playerId);

        List<BaseballPlayEvent> lifetimeBatting = tournamentId != null
                ? allBatting.stream().filter(e -> e.getMatch().getTournament() != null
                        && tournamentId.equals(e.getMatch().getTournament().getId()))
                        .collect(Collectors.toList())
                : allBatting;

        List<BaseballPlayEvent> lifetimePitching = tournamentId != null
                ? allPitching.stream().filter(e -> e.getMatch().getTournament() != null
                        && tournamentId.equals(e.getMatch().getTournament().getId()))
                        .collect(Collectors.toList())
                : allPitching;

        TournamentPlayerStatsDTO lifetime = buildStats(lifetimeBatting, lifetimePitching, sportType);

        Set<Long> tIds = allBatting.stream()
                .filter(e -> e.getMatch().getTournament() != null)
                .map(e -> e.getMatch().getTournament().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        allPitching.stream()
                .filter(e -> e.getMatch().getTournament() != null)
                .map(e -> e.getMatch().getTournament().getId())
                .forEach(tIds::add);

        List<TournamentBreakdownDTO> breakdown = new ArrayList<>();
        for (Long tid : tIds) {
            List<BaseballPlayEvent> tBat = allBatting.stream()
                    .filter(e -> e.getMatch().getTournament() != null && tid.equals(e.getMatch().getTournament().getId()))
                    .collect(Collectors.toList());
            List<BaseballPlayEvent> tPit = allPitching.stream()
                    .filter(e -> e.getMatch().getTournament() != null && tid.equals(e.getMatch().getTournament().getId()))
                    .collect(Collectors.toList());

            String tournamentName = (!tBat.isEmpty() ? tBat.get(0) : tPit.get(0))
                    .getMatch().getTournament().getName();
            breakdown.add(new TournamentBreakdownDTO(tid, tournamentName, buildStats(tBat, tPit, sportType)));
        }

        Team team = player.getTeam();
        PlayerStatisticsDetailDTO detail = new PlayerStatisticsDetailDTO();
        detail.setPlayerId(player.getId());
        detail.setPlayerName(player.getFullName());
        detail.setJerseyName(player.getJerseyName());
        detail.setJerseyNumber(player.getJerseyNumber());
        if (team != null) {
            detail.setTeamId(team.getId());
            detail.setTeamName(team.getName());
            detail.setTeamAcronym(team.getAcronym());
            detail.setTeamPhotoUrl(team.getPhotoUrl());
        }
        detail.setPlayerPhotoUrl(player.getPhotoUrl());
        detail.setLifetimeStats(lifetime);
        detail.setTournamentBreakdown(breakdown);
        return detail;
    }

    // ── Stats builder (shared logic) ─────────────────────────────────────────

    private TournamentPlayerStatsDTO buildStats(List<BaseballPlayEvent> batting,
                                                 List<BaseballPlayEvent> pitching,
                                                 SportType sportType) {
        TournamentPlayerStatsDTO dto = new TournamentPlayerStatsDTO();

        int singles    = count(batting, BaseballEventType.SINGLE);
        int doubles    = count(batting, BaseballEventType.DOUBLE);
        int triples    = count(batting, BaseballEventType.TRIPLE);
        int homeRuns   = count(batting, BaseballEventType.HOME_RUN);
        int hits       = singles + doubles + triples + homeRuns;
        int bb         = count(batting, BaseballEventType.WALK);
        int hbp        = count(batting, BaseballEventType.HIT_BY_PITCH);
        int sacFly     = count(batting, BaseballEventType.SACRIFICE_FLY);
        int strikeouts = count(batting, BaseballEventType.STRIKEOUT);
        int stolenBases = count(batting, BaseballEventType.STOLEN_BASE);
        int rbi        = batting.stream().mapToInt(e -> e.getRbi() != null ? e.getRbi() : 0).sum();
        int ab         = (int) batting.stream().filter(e -> AT_BAT_TYPES.contains(e.getEventType())).count();

        double obpVal = obp(hits, bb, hbp, ab, sacFly);
        double slgVal = ab > 0 ? (double) totalBases(singles, doubles, triples, homeRuns) / ab : 0.0;

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
        dto.setStolenBases(stolenBases);
        dto.setAvg(formatAvg(ab > 0 ? (double) hits / ab : 0.0));
        dto.setObp(formatAvg(obpVal));
        dto.setSlg(formatAvg(slgVal));
        dto.setOps(formatAvg(obpVal + slgVal));

        int totalOuts  = pitching.stream().mapToInt(this::outsFromEvent).sum();
        double ipDec   = totalOuts / 3.0;
        int pitchingK  = count(pitching, BaseballEventType.STRIKEOUT);
        int pitchingBB = count(pitching, BaseballEventType.WALK);
        int hAllowed   = (int) pitching.stream().filter(e -> HIT_TYPES.contains(e.getEventType())).count();
        int earnedRuns = pitching.stream().mapToInt(e -> e.getRunsScored() != null ? e.getRunsScored() : 0).sum();
        int appearances = (int) pitching.stream().map(e -> e.getMatch().getId()).distinct().count();

        // ERA multiplier: 7 innings for Softball, 9 for other sports (SOFTBALL_RULES.md §18)
        double eraMultiplier = sportType == SportType.SOFTBALL ? 7.0 : 9.0;

        dto.setPitchingAppearances(appearances);
        dto.setIp(formatIp(totalOuts));
        dto.setPitchingStrikeouts(pitchingK);
        dto.setPitchingWalks(pitchingBB);
        dto.setHitsAllowed(hAllowed);
        dto.setEarnedRuns(earnedRuns);
        dto.setEra(ipDec > 0 ? formatEra(earnedRuns * eraMultiplier / ipDec) : "0.00");
        dto.setWhip(ipDec > 0 ? formatEra((hAllowed + pitchingBB) / ipDec) : "0.00");
        return dto;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int count(List<BaseballPlayEvent> events, BaseballEventType type) {
        return (int) events.stream().filter(e -> e.getEventType() == type).count();
    }

    private int totalBases(int s, int d, int t, int hr) { return s + 2 * d + 3 * t + 4 * hr; }

    private double obp(int h, int bb, int hbp, int ab, int sf) {
        int denom = ab + bb + hbp + sf;
        return denom > 0 ? (double) (h + bb + hbp) / denom : 0.0;
    }

    private String formatAvg(double val) {
        if (val >= 1.0) return String.format("%.3f", val);
        String s = String.format("%.3f", val);
        return s.startsWith("0.") ? s.substring(1) : s;
    }

    private String formatEra(double val) { return String.format("%.2f", val); }
    private String formatIp(int outs) { return (outs / 3) + "." + (outs % 3); }

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
}
