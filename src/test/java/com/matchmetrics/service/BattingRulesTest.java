package com.matchmetrics.service;

import com.matchmetrics.domain.enums.BaseballEventType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class BattingRulesTest {

    // ── isHit ─────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0} isHit → true")
    @EnumSource(value = BaseballEventType.class, names = {"SINGLE", "DOUBLE", "TRIPLE", "HOME_RUN"})
    void isHit_trueForBaseHits(BaseballEventType type) {
        assertThat(type.isHit()).isTrue();
    }

    @ParameterizedTest(name = "{0} isHit → false")
    @EnumSource(value = BaseballEventType.class, names = {
        "WALK", "HIT_BY_PITCH", "SACRIFICE_FLY", "SACRIFICE_BUNT",
        "STRIKEOUT", "OUT", "DOUBLE_PLAY", "TRIPLE_PLAY", "ERROR",
        "STOLEN_BASE", "CAUGHT_STEALING", "PITCHING_CHANGE", "DEFENSIVE_CHANGE"
    })
    void isHit_falseForNonHits(BaseballEventType type) {
        assertThat(type.isHit()).isFalse();
    }

    // ── isAtBat ───────────────────────────────────────────────────

    @ParameterizedTest(name = "{0} isAtBat → true")
    @EnumSource(value = BaseballEventType.class, names = {
        "SINGLE", "DOUBLE", "TRIPLE", "HOME_RUN",
        "STRIKEOUT", "OUT", "DOUBLE_PLAY", "TRIPLE_PLAY", "ERROR"
    })
    void isAtBat_trueForAtBatEvents(BaseballEventType type) {
        assertThat(type.isAtBat()).isTrue();
    }

    @ParameterizedTest(name = "{0} isAtBat → false (no cuenta como turno al bate)")
    @EnumSource(value = BaseballEventType.class, names = {
        "WALK", "HIT_BY_PITCH", "SACRIFICE_FLY", "SACRIFICE_BUNT",
        "STOLEN_BASE", "CAUGHT_STEALING", "PITCHING_CHANGE", "DEFENSIVE_CHANGE",
        "PLATE_APPEARANCE", "RUN_SCORED", "COMMENT"
    })
    void isAtBat_falseForNonAtBatEvents(BaseballEventType type) {
        assertThat(type.isAtBat()).isFalse();
    }
}
