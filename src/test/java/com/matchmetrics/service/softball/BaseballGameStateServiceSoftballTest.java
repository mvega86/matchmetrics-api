package com.matchmetrics.service.softball;

import com.matchmetrics.domain.enums.BaseballEventType;
import com.matchmetrics.domain.enums.BaseballGameStatus;
import com.matchmetrics.domain.enums.InningHalf;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.mapper.BaseballGameStateMapper;
import com.matchmetrics.mapper.dto.BaseballGameStateDTO;
import com.matchmetrics.persistence.entity.BaseballGameState;
import com.matchmetrics.persistence.entity.BaseballPlayEvent;
import com.matchmetrics.persistence.entity.Match;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.repository.BaseballGameStateRepository;
import com.matchmetrics.persistence.repository.BaseballPlayEventRepository;
import com.matchmetrics.persistence.repository.MatchRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import com.matchmetrics.service.implementation.BaseballGameStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BaseballGameStateServiceSoftballTest {

    @Mock BaseballGameStateRepository gameStateRepository;
    @Mock BaseballPlayEventRepository playEventRepository;
    @Mock PlayerMatchRepository playerMatchRepository;
    @Mock MatchRepository matchRepository;
    @Mock BaseballGameStateMapper mapper;

    @InjectMocks BaseballGameStateService service;

    private BaseballGameState gameState;
    private Match match;
    private PlayerMatch batter1;
    private PlayerMatch batter2;
    private PlayerMatch batter3;

    @BeforeEach
    void setUp() {
        match = new Match();
        match.setId(1L);
        match.setSportType(SportType.SOFTBALL);

        batter1 = new PlayerMatch(); batter1.setId(101L);
        batter2 = new PlayerMatch(); batter2.setId(102L);
        batter3 = new PlayerMatch(); batter3.setId(103L);

        gameState = new BaseballGameState();
        gameState.setId(1L);
        gameState.setMatch(match);
        gameState.setCurrentInning(1);
        gameState.setInningHalf(InningHalf.TOP);
        gameState.setOuts(0);
        gameState.setBalls(0);
        gameState.setStrikes(0);
        gameState.setHomeScore(0);
        gameState.setAwayScore(0);
        gameState.setStatus(BaseballGameStatus.IN_PROGRESS);
        gameState.setTotalInnings(7);

        when(gameStateRepository.findByMatchId(1L)).thenReturn(Optional.of(gameState));
        when(gameStateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(new BaseballGameStateDTO());
    }

    // ── Helpers ──────────────────────────────────────────────────

    private BaseballPlayEvent event(BaseballEventType type, PlayerMatch batter,
                                    int outsOnPlay, int runsScored, InningHalf half) {
        BaseballPlayEvent ev = new BaseballPlayEvent();
        ev.setEventType(type);
        ev.setBatterPlayerMatch(batter);
        ev.setOutsOnPlay(outsOnPlay);
        ev.setRunsScored(runsScored);
        ev.setInningHalf(half);
        return ev;
    }

    private void assertBases(BaseballGameState gs,
                              PlayerMatch exp1, PlayerMatch exp2, PlayerMatch exp3) {
        assertThat(gs.getFirstBasePlayerMatch()).isEqualTo(exp1);
        assertThat(gs.getSecondBasePlayerMatch()).isEqualTo(exp2);
        assertThat(gs.getThirdBasePlayerMatch()).isEqualTo(exp3);
    }

    private BaseballGameState capturedSave() {
        ArgumentCaptor<BaseballGameState> cap = ArgumentCaptor.forClass(BaseballGameState.class);
        verify(gameStateRepository, atLeastOnce()).save(cap.capture());
        List<BaseballGameState> saved = cap.getAllValues();
        return saved.get(saved.size() - 1);
    }

    // ── rebuild — no events ──────────────────────────────────────

    @Test
    void rebuild_noEvents_returnsUnchangedState() {
        when(playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(Collections.emptyList());
        service.rebuildGameStateFromEvents(1L);
        verify(gameStateRepository, never()).save(any());
    }

    // ── rebuild — score reconstruction ──────────────────────────

    @Test
    void rebuild_scoreFromRunsScored_correctTotals() {
        List<BaseballPlayEvent> events = List.of(
            event(BaseballEventType.HOME_RUN, batter1, 0, 2, InningHalf.TOP),
            event(BaseballEventType.SINGLE,   batter2, 0, 1, InningHalf.BOTTOM)
        );
        when(playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(events);

        service.rebuildGameStateFromEvents(1L);

        BaseballGameState saved = capturedSave();
        assertThat(saved.getAwayScore()).isEqualTo(2);
        assertThat(saved.getHomeScore()).isEqualTo(1);
    }

    // ── rebuild — HOME_RUN clears bases ──────────────────────────

    @Test
    void rebuild_homeRun_clearsAllBases() {
        List<BaseballPlayEvent> events = List.of(
            event(BaseballEventType.HOME_RUN, batter1, 0, 1, InningHalf.TOP)
        );
        when(playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(events);

        service.rebuildGameStateFromEvents(1L);

        assertBases(capturedSave(), null, null, null);
    }

    // ── rebuild — SINGLE advances runners ────────────────────────

    @Test
    void rebuild_single_advancesRunners() {
        // batter1 WALK → 1B. Then batter2 SINGLE → batter2 on 1B, batter1 on 2B.
        List<BaseballPlayEvent> events = List.of(
            event(BaseballEventType.WALK,   batter1, 0, 0, InningHalf.TOP),
            event(BaseballEventType.SINGLE, batter2, 0, 0, InningHalf.TOP)
        );
        when(playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(events);

        service.rebuildGameStateFromEvents(1L);

        assertBases(capturedSave(), batter2, batter1, null);
    }

    // ── rebuild — DOUBLE advances runners ────────────────────────

    @Test
    void rebuild_double_advancesRunners() {
        // batter1 WALK → 1B. Then batter2 DOUBLE → batter2 on 2B, batter1 on 3B.
        List<BaseballPlayEvent> events = List.of(
            event(BaseballEventType.WALK,   batter1, 0, 0, InningHalf.TOP),
            event(BaseballEventType.DOUBLE, batter2, 0, 0, InningHalf.TOP)
        );
        when(playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(events);

        service.rebuildGameStateFromEvents(1L);

        assertBases(capturedSave(), null, batter2, batter1);
    }

    // ── rebuild — TRIPLE clears 1B/2B ────────────────────────────

    @Test
    void rebuild_triple_battersOnThirdOnly() {
        // batter1 WALK → 1B. batter2 WALK → 1B/2B. batter3 TRIPLE → batter3 on 3B, others score.
        List<BaseballPlayEvent> events = List.of(
            event(BaseballEventType.WALK,   batter1, 0, 0, InningHalf.TOP),
            event(BaseballEventType.WALK,   batter2, 0, 0, InningHalf.TOP),
            event(BaseballEventType.TRIPLE, batter3, 0, 2, InningHalf.TOP)
        );
        when(playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(events);

        service.rebuildGameStateFromEvents(1L);

        assertBases(capturedSave(), null, null, batter3);
    }

    // ── rebuild — WALK forced advance ────────────────────────────

    @Test
    void rebuild_walkBasesLoaded_forcesAllRunners() {
        // Fill bases: b1 1B, b2 2B, b3 3B via two walks, then b1 fills again after inning
        // Simpler: three consecutive walks to fill bases
        List<BaseballPlayEvent> events = List.of(
            event(BaseballEventType.WALK, batter1, 0, 0, InningHalf.TOP),
            event(BaseballEventType.WALK, batter2, 0, 0, InningHalf.TOP),
            event(BaseballEventType.WALK, batter3, 0, 0, InningHalf.TOP)
        );
        when(playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(events);

        service.rebuildGameStateFromEvents(1L);

        // After 3 walks: 1B=batter3, 2B=batter2, 3B=batter1 (no outs → no inning flip)
        BaseballGameState saved = capturedSave();
        assertThat(saved.getFirstBasePlayerMatch()).isEqualTo(batter3);
        assertThat(saved.getSecondBasePlayerMatch()).isEqualTo(batter2);
        assertThat(saved.getThirdBasePlayerMatch()).isEqualTo(batter1);
    }

    // ── rebuild — inning change clears bases ──────────────────────

    @Test
    void rebuild_threeOutsClearBases() {
        // batter1 WALK → 1B, then 3 strikeouts (one each from batter2, batter3, batter1)
        List<BaseballPlayEvent> events = List.of(
            event(BaseballEventType.WALK,      batter1, 0, 0, InningHalf.TOP),
            event(BaseballEventType.STRIKEOUT, batter2, 1, 0, InningHalf.TOP),
            event(BaseballEventType.STRIKEOUT, batter3, 1, 0, InningHalf.TOP),
            event(BaseballEventType.STRIKEOUT, batter1, 1, 0, InningHalf.TOP)
        );
        when(playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(events);

        service.rebuildGameStateFromEvents(1L);

        assertBases(capturedSave(), null, null, null);
    }

    // ── updateGameState — outs validation ────────────────────────

    @Test
    void updateGameState_outsOutOfRange_throwsException() {
        BaseballGameStateDTO dto = new BaseballGameStateDTO();
        dto.setOuts(3);

        assertThatThrownBy(() -> service.updateGameState(1L, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Outs must be between 0 and 2");
    }

    @Test
    void updateGameState_ballsOutOfRange_throwsException() {
        BaseballGameStateDTO dto = new BaseballGameStateDTO();
        dto.setBalls(4);

        assertThatThrownBy(() -> service.updateGameState(1L, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Balls must be between 0 and 3");
    }

    @Test
    void updateGameState_strikesOutOfRange_throwsException() {
        BaseballGameStateDTO dto = new BaseballGameStateDTO();
        dto.setStrikes(3);

        assertThatThrownBy(() -> service.updateGameState(1L, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Strikes must be between 0 and 2");
    }

    // ── updateGameState — duplicate runner on bases ───────────────

    @Test
    void updateGameState_samePlayerOnTwoBases_throwsException() {
        when(playerMatchRepository.existsByIdAndMatchId(101L, 1L)).thenReturn(true);
        when(playerMatchRepository.findById(101L)).thenReturn(Optional.of(batter1));

        BaseballGameStateDTO dto = new BaseballGameStateDTO();
        dto.setFirstBasePlayerMatchId(101L);
        dto.setSecondBasePlayerMatchId(101L);

        assertThatThrownBy(() -> service.updateGameState(1L, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot occupy more than one base");
    }
}
