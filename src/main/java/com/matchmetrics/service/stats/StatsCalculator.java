package com.matchmetrics.service.stats;

import com.matchmetrics.domain.enums.BaseballEventType;
import com.matchmetrics.persistence.entity.BaseballPlayEvent;

/**
 * Fórmulas estadísticas de béisbol/sóftbol centralizadas.
 * ERA softball usa multiplicador 7 (7 innings), béisbol usa 9 (9 innings).
 */
public final class StatsCalculator {

    private StatsCalculator() {}

    // ── Batting ───────────────────────────────────────────────────────────────

    public static double avg(int hits, int atBats) {
        return atBats > 0 ? (double) hits / atBats : 0.0;
    }

    public static double obp(int hits, int walks, int hitByPitch, int atBats, int sacFly) {
        int denom = atBats + walks + hitByPitch + sacFly;
        return denom > 0 ? (double) (hits + walks + hitByPitch) / denom : 0.0;
    }

    public static double slg(int singles, int doubles, int triples, int homeRuns, int atBats) {
        return atBats > 0 ? (double) totalBases(singles, doubles, triples, homeRuns) / atBats : 0.0;
    }

    public static int totalBases(int singles, int doubles, int triples, int homeRuns) {
        return singles + 2 * doubles + 3 * triples + 4 * homeRuns;
    }

    // ── Pitching ──────────────────────────────────────────────────────────────

    /** @param multiplier 7.0 para softball, 9.0 para béisbol */
    public static double era(int earnedRuns, double inningsPitched, double multiplier) {
        return inningsPitched > 0 ? earnedRuns * multiplier / inningsPitched : 0.0;
    }

    public static double whip(int hitsAllowed, int walks, double inningsPitched) {
        return inningsPitched > 0 ? (hitsAllowed + walks) / inningsPitched : 0.0;
    }

    public static int outsFromEvent(Integer outsOnPlay, BaseballEventType eventType) {
        int recorded = outsOnPlay != null ? outsOnPlay : 0;
        if (recorded > 0) return recorded;
        return switch (eventType) {
            case STRIKEOUT, OUT, CAUGHT_STEALING -> 1;
            case DOUBLE_PLAY -> 2;
            case TRIPLE_PLAY -> 3;
            default -> 0;
        };
    }

    public static int outsFromEvent(BaseballPlayEvent e) {
        return outsFromEvent(e.getOutsOnPlay(), e.getEventType());
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    /** Formato tipo batting average: elimina el "0" inicial si < 1 (.333 en vez de 0.333) */
    public static String formatAvg(double val) {
        if (val >= 1.0) return String.format("%.3f", val);
        String s = String.format("%.3f", val);
        return s.startsWith("0.") ? s.substring(1) : s;
    }

    public static String formatEra(double val) {
        return String.format("%.2f", val);
    }

    /** Innings pitcheados en formato "entradas.outs": 7 outs → "2.1" */
    public static String formatIp(int outs) {
        return (outs / 3) + "." + (outs % 3);
    }
}
