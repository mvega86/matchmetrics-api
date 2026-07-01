package com.matchmetrics.service.stats;

import com.matchmetrics.domain.enums.BaseballEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class StatsCalculatorTest {

    // ── avg ──────────────────────────────────────────────────────────────────

    @Test
    void avg_returnsZero_whenAtBatsIsZero() {
        assertThat(StatsCalculator.avg(0, 0)).isEqualTo(0.0);
    }

    @Test
    void avg_calculatesCorrectly() {
        assertThat(StatsCalculator.avg(3, 10)).isCloseTo(0.300, within(0.0001));
    }

    @Test
    void avg_perfectBatting_returnsOne() {
        assertThat(StatsCalculator.avg(4, 4)).isCloseTo(1.0, within(0.0001));
    }

    // ── obp ──────────────────────────────────────────────────────────────────

    @Test
    void obp_returnsZero_whenAllInputsAreZero() {
        assertThat(StatsCalculator.obp(0, 0, 0, 0, 0)).isEqualTo(0.0);
    }

    @Test
    void obp_includesSacFlyInDenominatorOnly() {
        // 2H, 0BB, 0HBP, 8AB, 2SF → (2)/(8+2) = .200
        assertThat(StatsCalculator.obp(2, 0, 0, 8, 2)).isCloseTo(0.200, within(0.0001));
    }

    @Test
    void obp_includesWalksAndHbpInNumeratorAndDenominator() {
        // 3H, 2BB, 1HBP, 10AB, 0SF → (3+2+1)/(10+2+1) = 6/13
        assertThat(StatsCalculator.obp(3, 2, 1, 10, 0)).isCloseTo(6.0 / 13, within(0.0001));
    }

    @Test
    void obp_sacFlyDoesNotCountAsHitOrPA_inNumerator() {
        // Solo sacFly, sin hits ni BB: denominador > 0 pero numerador = 0 → .000
        assertThat(StatsCalculator.obp(0, 0, 0, 0, 5)).isEqualTo(0.0);
    }

    // ── totalBases ───────────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0}×1B + {1}×2B + {2}×3B + {3}×HR = {4} TB")
    @CsvSource({
        "1, 0, 0, 0, 1",
        "0, 1, 0, 0, 2",
        "0, 0, 1, 0, 3",
        "0, 0, 0, 1, 4",
        "2, 1, 1, 1, 11",
        "0, 0, 0, 0, 0",
    })
    void totalBases_calculatesCorrectly(int s, int d, int t, int hr, int expected) {
        assertThat(StatsCalculator.totalBases(s, d, t, hr)).isEqualTo(expected);
    }

    // ── slg ──────────────────────────────────────────────────────────────────

    @Test
    void slg_returnsZero_whenAtBatsIsZero() {
        assertThat(StatsCalculator.slg(0, 0, 0, 0, 0)).isEqualTo(0.0);
    }

    @Test
    void slg_homeRunOnly_returnsFour() {
        // 1HR / 1AB = 4 bases / 1 = 4.000
        assertThat(StatsCalculator.slg(0, 0, 0, 1, 1)).isCloseTo(4.0, within(0.0001));
    }

    @Test
    void slg_mixedHits_calculatesCorrectly() {
        // 1×1B + 1×2B + 0×3B + 0×HR / 4 AB = (1+2)/4 = .750
        assertThat(StatsCalculator.slg(1, 1, 0, 0, 4)).isCloseTo(0.750, within(0.0001));
    }

    // ── era ──────────────────────────────────────────────────────────────────

    @Test
    void era_softball_usesMultiplier7() {
        // 7 ER en 7 IP → ERA = 7*7/7 = 7.00
        assertThat(StatsCalculator.era(7, 7.0, 7.0)).isCloseTo(7.0, within(0.01));
    }

    @Test
    void era_softball_typicalCase() {
        // 3 ER en 7 IP (partido completo) → ERA = 3*7/7 = 3.00
        assertThat(StatsCalculator.era(3, 7.0, 7.0)).isCloseTo(3.0, within(0.01));
    }

    @Test
    void era_baseball_usesMultiplier9() {
        // 3 ER en 9 IP → ERA = 3*9/9 = 3.00
        assertThat(StatsCalculator.era(3, 9.0, 9.0)).isCloseTo(3.0, within(0.01));
    }

    @Test
    void era_returnsZero_whenNoPitchingInnings() {
        assertThat(StatsCalculator.era(5, 0.0, 7.0)).isEqualTo(0.0);
    }

    @Test
    void era_zeroEarnedRuns_returnsZero() {
        assertThat(StatsCalculator.era(0, 7.0, 7.0)).isEqualTo(0.0);
    }

    // ── whip ─────────────────────────────────────────────────────────────────

    @Test
    void whip_returnsZero_whenNoPitchingInnings() {
        assertThat(StatsCalculator.whip(3, 2, 0.0)).isEqualTo(0.0);
    }

    @Test
    void whip_calculatesCorrectly() {
        // 3H + 2BB en 5 IP → WHIP = 5/5 = 1.00
        assertThat(StatsCalculator.whip(3, 2, 5.0)).isCloseTo(1.0, within(0.001));
    }

    // ── formatAvg ────────────────────────────────────────────────────────────

    @Test
    void formatAvg_removesLeadingZero_belowOne() {
        assertThat(StatsCalculator.formatAvg(0.333)).isEqualTo(".333");
    }

    @Test
    void formatAvg_keepsFullFormat_atOrAboveOne() {
        assertThat(StatsCalculator.formatAvg(1.0)).isEqualTo("1.000");
    }

    @Test
    void formatAvg_zero_producesZeroString() {
        assertThat(StatsCalculator.formatAvg(0.0)).isEqualTo(".000");
    }

    @Test
    void formatAvg_rounds_correctlyAtThirdDecimal() {
        assertThat(StatsCalculator.formatAvg(0.3335)).isEqualTo(".334");
    }

    // ── formatIp ─────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0} outs → \"{1}\"")
    @CsvSource({
        "0,  0.0",
        "1,  0.1",
        "2,  0.2",
        "3,  1.0",
        "4,  1.1",
        "5,  1.2",
        "21, 7.0",
        "27, 9.0",
    })
    void formatIp_calculatesCorrectly(int outs, String expected) {
        assertThat(StatsCalculator.formatIp(outs)).isEqualTo(expected);
    }

    // ── outsFromEvent (raw params) ────────────────────────────────────────────

    @Test
    void outsFromEvent_explicit_outsOnPlay_takePrecedence() {
        assertThat(StatsCalculator.outsFromEvent(2, BaseballEventType.SINGLE)).isEqualTo(2);
    }

    @ParameterizedTest(name = "null outsOnPlay + {0} → {1} out(s)")
    @CsvSource({
        "STRIKEOUT,      1",
        "OUT,            1",
        "CAUGHT_STEALING, 1",
        "DOUBLE_PLAY,    2",
        "TRIPLE_PLAY,    3",
        "SINGLE,         0",
        "HOME_RUN,       0",
        "WALK,           0",
    })
    void outsFromEvent_nullOutsOnPlay_derivesFromEventType(BaseballEventType type, int expected) {
        assertThat(StatsCalculator.outsFromEvent(null, type)).isEqualTo(expected);
    }

    @Test
    void outsFromEvent_zeroOutsOnPlay_derivesFromEventType() {
        assertThat(StatsCalculator.outsFromEvent(0, BaseballEventType.STRIKEOUT)).isEqualTo(1);
    }
}
