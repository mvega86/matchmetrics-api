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

    // ── Public API ────────────────────────────────────────────────────────────

    @Override
    public TournamentPlayerStatsDTO getPlayerTournamentStats(Long playerMatchId) {
        PlayerMatch pm = findPlayerMatch(playerMatchId);
        Long playerId     = pm.getPlayer().getId();
        Long tournamentId = pm.getMatch().getTournament() != null
                ? pm.getMatch().getTournament().getId()
                : null;
        if (tournamentId == null) return emptyStats();

        return buildStats(
                playEventRepository.findBattingEventsByTournamentAndPlayer(tournamentId, playerId),
                playEventRepository.findPitchingEventsByTournamentAndPlayer(tournamentId, playerId)
        );
    }

    @Override
    public TournamentPlayerStatsDTO getPlayerLifetimeStats(Long playerMatchId) {
        PlayerMatch pm    = findPlayerMatch(playerMatchId);
        Long playerId     = pm.getPlayer().getId();

        return buildStats(
                playEventRepository.findBattingEventsByPlayer(playerId),
                playEventRepository.findPitchingEventsByPlayer(playerId)
        );
    }

    // ── Core stats builder ────────────────────────────────────────────────────

    private TournamentPlayerStatsDTO buildStats(List<BaseballPlayEvent> battingEvents,
                                                 List<BaseballPlayEvent> pitchingEvents) {
        TournamentPlayerStatsDTO dto = new TournamentPlayerStatsDTO();

        // ── Batting ──────────────────────────────────────────────────────────
        int singles    = count(battingEvents, BaseballEventType.SINGLE);
        int doubles    = count(battingEvents, BaseballEventType.DOUBLE);
        int triples    = count(battingEvents, BaseballEventType.TRIPLE);
        int homeRuns   = count(battingEvents, BaseballEventType.HOME_RUN);
        int hits       = singles + doubles + triples + homeRuns;
        int bb         = count(battingEvents, BaseballEventType.WALK);
        int hbp        = count(battingEvents, BaseballEventType.HIT_BY_PITCH);
        int sacFly     = count(battingEvents, BaseballEventType.SACRIFICE_FLY);
        int strikeouts = count(battingEvents, BaseballEventType.STRIKEOUT);
        int rbi        = battingEvents.stream().mapToInt(e -> e.getRbi() != null ? e.getRbi() : 0).sum();
        int ab         = (int) battingEvents.stream().filter(e -> AT_BAT_TYPES.contains(e.getEventType())).count();

        dto.setAb(ab);           dto.setHits(hits);      dto.setSingles(singles);
        dto.setDoubles(doubles); dto.setTriples(triples); dto.setHomeRuns(homeRuns);
        dto.setRbi(rbi);        dto.setWalks(bb);        dto.setHitByPitch(hbp);
        dto.setStrikeouts(strikeouts);
        dto.setAvg(formatAvg(ab > 0 ? (double) hits / ab : 0.0));
        dto.setObp(formatAvg(obp(hits, bb, hbp, ab, sacFly)));
        dto.setSlg(formatAvg(ab > 0 ? (double) totalBases(singles, doubles, triples, homeRuns) / ab : 0.0));

        // ── Pitching ─────────────────────────────────────────────────────────
        int totalOuts   = pitchingEvents.stream().mapToInt(this::outsFromEvent).sum();
        double ipDec    = totalOuts / 3.0;
        int pitchingK   = count(pitchingEvents, BaseballEventType.STRIKEOUT);
        int pitchingBB  = count(pitchingEvents, BaseballEventType.WALK);
        int hitsAllowed = (int) pitchingEvents.stream().filter(e -> HIT_TYPES.contains(e.getEventType())).count();
        int earnedRuns  = pitchingEvents.stream().mapToInt(e -> e.getRunsScored() != null ? e.getRunsScored() : 0).sum();
        int appearances = (int) pitchingEvents.stream().map(e -> e.getMatch().getId()).distinct().count();

        int[] wls = computeWLS(pitchingEvents);

        dto.setPitchingAppearances(appearances);
        dto.setWins(wls[0]);   dto.setLosses(wls[1]); dto.setSaves(wls[2]);
        dto.setIp(formatIp(totalOuts));
        dto.setPitchingStrikeouts(pitchingK); dto.setPitchingWalks(pitchingBB);
        dto.setHitsAllowed(hitsAllowed);      dto.setEarnedRuns(earnedRuns);
        dto.setEra(ipDec > 0 ? formatEra(earnedRuns * 9.0 / ipDec) : "0.00");
        dto.setWhip(ipDec > 0 ? formatEra((hitsAllowed + pitchingBB) / ipDec) : "0.00");

        return dto;
    }

    // ── W / L / S ─────────────────────────────────────────────────────────────

    private int[] computeWLS(List<BaseballPlayEvent> pitchingEvents) {
        Map<Long, List<BaseballPlayEvent>> byMatch = pitchingEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getMatch().getId()));

        int wins = 0, losses = 0, saves = 0;
        for (List<BaseballPlayEvent> matchEvents : byMatch.values()) {
            BaseballPlayEvent sample = matchEvents.get(0);
            Match match = sample.getMatch();
            if (match.getState() != MatchState.FINISHED) continue;
            // Partido empatado al final: no se asigna decisión (correcto según reglas)
            if (match.getHomeScore() == match.getAwayScore()) continue;

            Long pitcherPlayerId = sample.getPitcherPlayerMatch().getPlayer().getId();
            List<BaseballPlayEvent> allEvents =
                    playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(match.getId());

            MatchWLS wls = resolveMatchWLS(allEvents, match);
            if (pitcherPlayerId.equals(wls.winnerId())) wins++;
            if (pitcherPlayerId.equals(wls.loserId()))  losses++;
            if (pitcherPlayerId.equals(wls.saverId()))  saves++;
        }
        return new int[]{wins, losses, saves};
    }

    private record MatchWLS(Long winnerId, Long loserId, Long saverId) {}

    /**
     * Simula el partido cronológicamente para resolver exactamente qué pitcher
     * ganó, perdió y salvó, siguiendo las reglas oficiales de softball/béisbol.
     *
     * Manejo de pitcherPlayerMatch nulo (limitación del operador):
     *   Cuando un evento no tiene pitcher explícito, se usa el último pitcher
     *   conocido del equipo en campo (propagación hacia adelante). Esto garantiza
     *   que si el operador olvidó asignar el pitcher en alguna jugada, las
     *   estadísticas de W/L/S siguen siendo correctas.
     *
     * Partido empatado al final:
     *   No se asigna decisión a ningún pitcher (comportamiento correcto según
     *   las reglas; en softball normalmente no ocurre por las entradas extra).
     */
    private MatchWLS resolveMatchWLS(List<BaseballPlayEvent> allEvents, Match match) {
        Long homeId = match.getHomeTeam().getId();
        Long awayId = match.getAwayTeam().getId();
        boolean homeWins  = match.getHomeScore() > match.getAwayScore();
        Long winnerTeamId = homeWins ? homeId : awayId;

        int n = allEvents.size();

        // ── Pitcher efectivo por evento (fallback al último conocido) ─────────
        // Resuelve la limitación de pitcherPlayerMatch nulo: si el operador no
        // asignó el pitcher en un evento, se hereda el último pitcher explícito
        // del mismo equipo en campo. Cada equipo tiene su propia cadena de
        // propagación para no mezclar pitchers de equipos distintos.
        Long lastHomePitcher = null;
        Long lastAwayPitcher = null;
        Long[] effectivePitcher = new Long[n];

        for (int i = 0; i < n; i++) {
            BaseballPlayEvent e = allEvents.get(i);
            if (e.getFieldingTeam() == null) continue;

            Long explicit = e.getPitcherPlayerMatch() != null
                    ? e.getPitcherPlayerMatch().getPlayer().getId()
                    : null;

            if (homeId.equals(e.getFieldingTeam().getId())) {
                if (explicit != null) lastHomePitcher = explicit;
                effectivePitcher[i] = lastHomePitcher;
            } else if (awayId.equals(e.getFieldingTeam().getId())) {
                if (explicit != null) lastAwayPitcher = explicit;
                effectivePitcher[i] = lastAwayPitcher;
            }
        }

        // ── Marcador acumulado tras cada evento ───────────────────────────────
        int[] homeAfter = new int[n];
        int[] awayAfter = new int[n];
        int h = 0, a = 0;
        for (int i = 0; i < n; i++) {
            BaseballPlayEvent e = allEvents.get(i);
            int runs = e.getRunsScored() != null ? e.getRunsScored() : 0;
            if (runs > 0 && e.getBattingTeam() != null) {
                if (homeId.equals(e.getBattingTeam().getId()))      h += runs;
                else if (awayId.equals(e.getBattingTeam().getId())) a += runs;
            }
            homeAfter[i] = h;
            awayAfter[i] = a;
        }

        // ── Ventaja definitiva ────────────────────────────────────────────────
        // Primera jugada con carreras donde el ganador se pone al frente y ya
        // nunca vuelve a perder esa ventaja ni empatar el marcador.
        int decisiveIdx = -1;
        for (int i = 0; i < n; i++) {
            int runs = allEvents.get(i).getRunsScored() != null ? allEvents.get(i).getRunsScored() : 0;
            if (runs <= 0) continue;

            boolean ahead = homeWins ? homeAfter[i] > awayAfter[i] : awayAfter[i] > homeAfter[i];
            if (!ahead) continue;

            boolean permanent = true;
            for (int j = i + 1; j < n; j++) {
                boolean still = homeWins ? homeAfter[j] > awayAfter[j] : awayAfter[j] > homeAfter[j];
                if (!still) { permanent = false; break; }
            }
            if (permanent) { decisiveIdx = i; break; }
        }

        if (decisiveIdx < 0) return new MatchWLS(null, null, null);

        // ── LOSS: pitcher del equipo perdedor en el evento decisivo ───────────
        // Usa effectivePitcher para tolerar eventos sin pitcher explícito.
        Long loserId = effectivePitcher[decisiveIdx];

        // ── WIN: último pitcher del equipo ganador hasta el evento decisivo ───
        // El ganador estaba al bate en el evento decisivo, no en el montículo;
        // su pitcher es el último que lanzó antes de ese momento.
        Long winnerId = null;
        for (int i = decisiveIdx; i >= 0; i--) {
            BaseballPlayEvent e = allEvents.get(i);
            if (e.getFieldingTeam() == null) continue;
            if (winnerTeamId.equals(e.getFieldingTeam().getId()) && effectivePitcher[i] != null) {
                winnerId = effectivePitcher[i];
                break;
            }
        }

        // ── CLOSER: último pitcher del equipo ganador en todo el partido ──────
        Long closerId = null;
        for (int i = n - 1; i >= 0; i--) {
            BaseballPlayEvent e = allEvents.get(i);
            if (e.getFieldingTeam() == null) continue;
            if (winnerTeamId.equals(e.getFieldingTeam().getId()) && effectivePitcher[i] != null) {
                closerId = effectivePitcher[i];
                break;
            }
        }

        // ── SAVE: cerrador ≠ ganador y cumple situación de salvamento ─────────
        Long saverId = null;
        if (closerId != null && !closerId.equals(winnerId)
                && isSaveSituation(allEvents, effectivePitcher, homeAfter, awayAfter,
                                   closerId, homeWins, winnerTeamId)) {
            saverId = closerId;
        }

        return new MatchWLS(winnerId, loserId, saverId);
    }

    /**
     * Verifica si el cerrador entró en situación de salvamento.
     * Condiciones (basta cumplir una):
     *  1. Entró con ventaja de 3 carreras o menos.
     *  2. La carrera del empate estaba al bate o en base al entrar.
     *  3. Lanzó al menos 3 innings efectivos para cerrar el partido.
     *
     * Para las condiciones 1 y 2 se usa el marcador justo antes del primer
     * evento que lanzó el cerrador y el estado de bases de ese primer pitcheo.
     * Es la mejor aproximación posible con los datos disponibles por evento.
     */
    private boolean isSaveSituation(List<BaseballPlayEvent> allEvents, Long[] effectivePitcher,
                                     int[] homeAfter, int[] awayAfter,
                                     Long closerId, boolean homeWins, Long winnerTeamId) {
        // Primer índice donde el cerrador aparece como pitcher del equipo ganador
        int entryIdx = -1;
        for (int i = 0; i < allEvents.size(); i++) {
            BaseballPlayEvent e = allEvents.get(i);
            if (e.getFieldingTeam() == null) continue;
            if (closerId.equals(effectivePitcher[i])
                    && winnerTeamId.equals(e.getFieldingTeam().getId())) {
                entryIdx = i;
                break;
            }
        }
        if (entryIdx < 0) return false;

        // Marcador inmediatamente antes de su primera jugada
        int hBefore = entryIdx > 0 ? homeAfter[entryIdx - 1] : 0;
        int aBefore = entryIdx > 0 ? awayAfter[entryIdx - 1] : 0;
        int lead    = homeWins ? (hBefore - aBefore) : (aBefore - hBefore);

        // Condición 1: ventaja ≤ 3 carreras al entrar
        if (lead > 0 && lead <= 3) return true;

        // Condición 2: carrera del empate al bate o en base al entrar.
        // Se usa el estado de bases del primer pitcheo del cerrador, que coincide
        // con el inicio del turno al bate (en softball los pitchers casi siempre
        // entran entre turnos, por lo que esta aproximación es prácticamente exacta).
        BaseballPlayEvent firstPitch = allEvents.get(entryIdx);
        int runnersOn = (Boolean.TRUE.equals(firstPitch.getFirstBaseBefore())  ? 1 : 0)
                      + (Boolean.TRUE.equals(firstPitch.getSecondBaseBefore()) ? 1 : 0)
                      + (Boolean.TRUE.equals(firstPitch.getThirdBaseBefore())  ? 1 : 0);
        // Si la ventaja ≤ corredores en base + bateador actual, la carrera del
        // empate está en base o al bate.
        if (lead > 0 && lead <= runnersOn + 1) return true;

        // Condición 3: ≥ 3 innings efectivos (≥ 9 outs registrados)
        int outsRecorded = 0;
        for (int i = 0; i < allEvents.size(); i++) {
            BaseballPlayEvent e = allEvents.get(i);
            if (e.getFieldingTeam() == null) continue;
            if (closerId.equals(effectivePitcher[i])
                    && winnerTeamId.equals(e.getFieldingTeam().getId())) {
                outsRecorded += outsFromEvent(e);
            }
        }
        return outsRecorded >= 9;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PlayerMatch findPlayerMatch(Long playerMatchId) {
        return playerMatchRepository.findById(playerMatchId)
                .orElseThrow(() -> new IllegalArgumentException("PlayerMatch not found: " + playerMatchId));
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
    private String formatIp(int outs)    { return (outs / 3) + "." + (outs % 3); }

    private TournamentPlayerStatsDTO emptyStats() {
        TournamentPlayerStatsDTO dto = new TournamentPlayerStatsDTO();
        dto.setAvg(".000"); dto.setObp(".000"); dto.setSlg(".000");
        dto.setIp("0.0");  dto.setEra("0.00"); dto.setWhip("0.00");
        return dto;
    }
}
