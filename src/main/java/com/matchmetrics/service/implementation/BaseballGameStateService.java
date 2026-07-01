package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.BaseballEventType;
import com.matchmetrics.domain.enums.BaseballGameStatus;
import com.matchmetrics.domain.enums.InningHalf;
import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.exception.ConflictException;
import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.BaseballGameStateMapper;
import com.matchmetrics.mapper.dto.BaseballGameStateDTO;
import com.matchmetrics.persistence.entity.BaseballGameState;
import com.matchmetrics.persistence.entity.BaseballPlayEvent;
import com.matchmetrics.persistence.entity.Match;
import com.matchmetrics.persistence.entity.PitcherPitchCount;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.repository.BaseballGameStateRepository;
import com.matchmetrics.persistence.repository.BaseballPlayEventRepository;
import com.matchmetrics.persistence.repository.MatchRepository;
import com.matchmetrics.persistence.repository.PitcherPitchCountRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import com.matchmetrics.service.IBaseballGameStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class BaseballGameStateService implements IBaseballGameStateService {

    private static final Set<BaseballEventType> AT_BAT_ENDING = Set.of(
        BaseballEventType.SINGLE, BaseballEventType.DOUBLE, BaseballEventType.TRIPLE,
        BaseballEventType.HOME_RUN, BaseballEventType.WALK, BaseballEventType.STRIKEOUT,
        BaseballEventType.OUT, BaseballEventType.DOUBLE_PLAY, BaseballEventType.TRIPLE_PLAY,
        BaseballEventType.HIT_BY_PITCH, BaseballEventType.ERROR,
        BaseballEventType.SACRIFICE_FLY, BaseballEventType.SACRIFICE_BUNT
    );

    private final BaseballGameStateRepository gameStateRepository;
    private final BaseballPlayEventRepository playEventRepository;
    private final PlayerMatchRepository playerMatchRepository;
    private final MatchRepository matchRepository;
    private final PitcherPitchCountRepository pitcherPitchCountRepository;
    private final BaseballGameStateMapper mapper;

    public BaseballGameStateService(
            BaseballGameStateRepository gameStateRepository,
            BaseballPlayEventRepository playEventRepository,
            PlayerMatchRepository playerMatchRepository,
            MatchRepository matchRepository,
            PitcherPitchCountRepository pitcherPitchCountRepository,
            BaseballGameStateMapper mapper
    ) {
        this.gameStateRepository = gameStateRepository;
        this.playEventRepository = playEventRepository;
        this.playerMatchRepository = playerMatchRepository;
        this.matchRepository = matchRepository;
        this.pitcherPitchCountRepository = pitcherPitchCountRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#dto.matchId")
    public BaseballGameStateDTO createGameState(BaseballGameStateDTO dto) {
        log.info("Creating baseball game state for match: {}", dto.getMatchId());

        if (gameStateRepository.existsByMatchId(dto.getMatchId())) {
            log.error("Game state already exists for match: {}", dto.getMatchId());
            throw new ConflictException("Game state already exists for this match");
        }

        try {
            BaseballGameState gameState = mapper.toEntity(dto);
            gameState.setStatus(BaseballGameStatus.NOT_STARTED);
            int totalInnings = gameState.getMatch().getSportType() == SportType.SOFTBALL ? 7 : 9;
            gameState.setTotalInnings(totalInnings);
            gameState = gameStateRepository.save(gameState);
            log.info("Game state created for match: {} ({} innings)", dto.getMatchId(), totalInnings);
            return mapper.toDTO(gameState);
        } catch (Exception e) {
            log.error("Error creating game state: {}", e.getMessage());
            throw new RuntimeException("Error creating game state");
        }
    }

    @Override
    @Cacheable(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO getGameStateByMatchId(Long matchId) {
        log.info("Retrieving game state for match: {}", matchId);
        BaseballGameState gameState = gameStateRepository.findByMatchId(matchId)
                .orElseThrow(() -> {
                    log.error("Game state not found for match: {}", matchId);
                    return new EntityNotFoundException("Game state not found for match: " + matchId);
                });
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO updateGameState(Long matchId, BaseballGameStateDTO dto) {
        log.info("Updating game state for match: {}", matchId);
        BaseballGameState gameState = getGameStateEntity(matchId);

        if (dto.getCurrentInning() != null) {
            gameState.setCurrentInning(dto.getCurrentInning());
        }
        if (dto.getInningHalf() != null) {
            gameState.setInningHalf(dto.getInningHalf());
        }
        if (dto.getOuts() != null) {
            if (dto.getOuts() < 0 || dto.getOuts() > 2) {
                throw new IllegalArgumentException("Outs must be between 0 and 2");
            }
            gameState.setOuts(dto.getOuts());
        }
        if (dto.getBalls() != null) {
            if (dto.getBalls() < 0 || dto.getBalls() > 3) {
                throw new IllegalArgumentException("Balls must be between 0 and 3");
            }
            gameState.setBalls(dto.getBalls());
        }
        if (dto.getStrikes() != null) {
            if (dto.getStrikes() < 0 || dto.getStrikes() > 2) {
                throw new IllegalArgumentException("Strikes must be between 0 and 2");
            }
            gameState.setStrikes(dto.getStrikes());
        }
        if (dto.getHomeScore() != null) {
            gameState.setHomeScore(dto.getHomeScore());
        }
        if (dto.getAwayScore() != null) {
            gameState.setAwayScore(dto.getAwayScore());
        }
        if (Boolean.TRUE.equals(dto.getClearFirstBase())) {
            gameState.setFirstBasePlayerMatch(null);
        } else if (dto.getFirstBasePlayerMatchId() != null) {
            if (!playerMatchRepository.existsByIdAndMatchId(dto.getFirstBasePlayerMatchId(), matchId)) {
                throw new IllegalArgumentException("PlayerMatch " + dto.getFirstBasePlayerMatchId() + " does not belong to match " + matchId);
            }
            gameState.setFirstBasePlayerMatch(playerMatchRepository.findById(dto.getFirstBasePlayerMatchId()).orElse(null));
        }
        if (Boolean.TRUE.equals(dto.getClearSecondBase())) {
            gameState.setSecondBasePlayerMatch(null);
        } else if (dto.getSecondBasePlayerMatchId() != null) {
            if (!playerMatchRepository.existsByIdAndMatchId(dto.getSecondBasePlayerMatchId(), matchId)) {
                throw new IllegalArgumentException("PlayerMatch " + dto.getSecondBasePlayerMatchId() + " does not belong to match " + matchId);
            }
            gameState.setSecondBasePlayerMatch(playerMatchRepository.findById(dto.getSecondBasePlayerMatchId()).orElse(null));
        }
        if (Boolean.TRUE.equals(dto.getClearThirdBase())) {
            gameState.setThirdBasePlayerMatch(null);
        } else if (dto.getThirdBasePlayerMatchId() != null) {
            if (!playerMatchRepository.existsByIdAndMatchId(dto.getThirdBasePlayerMatchId(), matchId)) {
                throw new IllegalArgumentException("PlayerMatch " + dto.getThirdBasePlayerMatchId() + " does not belong to match " + matchId);
            }
            gameState.setThirdBasePlayerMatch(playerMatchRepository.findById(dto.getThirdBasePlayerMatchId()).orElse(null));
        }
        if (Boolean.TRUE.equals(dto.getClearCurrentPitcher())) {
            gameState.setCurrentPitcherPlayerMatch(null);
            gameState.setPitchCount(0);
        } else if (dto.getCurrentPitcherPlayerMatchId() != null) {
            if (!playerMatchRepository.existsByIdAndMatchId(dto.getCurrentPitcherPlayerMatchId(), matchId)) {
                throw new IllegalArgumentException(
                    "PlayerMatch " + dto.getCurrentPitcherPlayerMatchId() + " does not belong to match " + matchId);
            }
            PlayerMatch pitcher = playerMatchRepository.findById(dto.getCurrentPitcherPlayerMatchId())
                    .orElseThrow(() -> new EntityNotFoundException("PlayerMatch not found: " + dto.getCurrentPitcherPlayerMatchId()));
            gameState.setCurrentPitcherPlayerMatch(pitcher);
        }
        if (dto.getPitchCount() != null) {
            int newCount = Math.max(0, dto.getPitchCount());
            gameState.setPitchCount(newCount);
            if (gameState.getCurrentPitcherPlayerMatch() != null) {
                final BaseballGameState gs = gameState;
                final int count = newCount;
                PitcherPitchCount record = pitcherPitchCountRepository
                    .findByGameStateIdAndPitcherPMId(gs.getId(), gs.getCurrentPitcherPlayerMatch().getId())
                    .orElseGet(() -> {
                        PitcherPitchCount r = new PitcherPitchCount();
                        r.setGameState(gs);
                        r.setPitcherPlayerMatch(gs.getCurrentPitcherPlayerMatch());
                        return r;
                    });
                record.setPitchCount(count);
                pitcherPitchCountRepository.save(record);
            }
        }
        if (Boolean.TRUE.equals(dto.getClearCurrentBatter())) {
            gameState.setCurrentBatterPlayerMatch(null);
        } else if (dto.getCurrentBatterPlayerMatchId() != null) {
            if (!playerMatchRepository.existsByIdAndMatchId(dto.getCurrentBatterPlayerMatchId(), matchId)) {
                throw new IllegalArgumentException("PlayerMatch " + dto.getCurrentBatterPlayerMatchId() + " does not belong to match " + matchId);
            }
            gameState.setCurrentBatterPlayerMatch(playerMatchRepository.findById(dto.getCurrentBatterPlayerMatchId()).orElse(null));
        }
        if (dto.getStatus() != null) {
            gameState.setStatus(dto.getStatus());
            // When the game finishes, persist the final score and state on the Match record
            if (dto.getStatus() == BaseballGameStatus.FINISHED) {
                syncFinishedGameToMatch(gameState);
            }
        }

        validateNoDuplicateRunners(gameState);

        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    private void validateNoDuplicateRunners(BaseballGameState state) {
        Long first  = state.getFirstBasePlayerMatch()  != null ? state.getFirstBasePlayerMatch().getId()  : null;
        Long second = state.getSecondBasePlayerMatch() != null ? state.getSecondBasePlayerMatch().getId() : null;
        Long third  = state.getThirdBasePlayerMatch()  != null ? state.getThirdBasePlayerMatch().getId()  : null;

        if (first  != null && (first.equals(second) || first.equals(third))) {
            throw new IllegalArgumentException("Player " + first + " cannot occupy more than one base at the same time");
        }
        if (second != null && second.equals(third)) {
            throw new IllegalArgumentException("Player " + second + " cannot occupy more than one base at the same time");
        }
    }

    private void syncFinishedGameToMatch(BaseballGameState gameState) {
        Match match = gameState.getMatch();
        if (match == null) return;
        match.setHomeScore(gameState.getHomeScore() != null ? gameState.getHomeScore() : 0);
        match.setAwayScore(gameState.getAwayScore() != null ? gameState.getAwayScore() : 0);
        match.setState(MatchState.FINISHED);
        matchRepository.save(match);
        log.info("Match {} result synced: home={} away={}", match.getId(),
            match.getHomeScore(), match.getAwayScore());
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO updateInning(Long matchId, Integer inning) {
        log.info("Updating inning for match: {} to inning: {}", matchId, inning);
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setCurrentInning(inning);
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO updateInningHalf(Long matchId, String inningHalf) {
        log.info("Updating inning half for match: {} to: {}", matchId, inningHalf);
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setInningHalf(InningHalf.valueOf(inningHalf.toUpperCase()));
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO updateOuts(Long matchId, Integer outs) {
        log.info("Updating outs for match: {} to: {}", matchId, outs);
        if (outs < 0 || outs > 2) {
            log.error("Invalid outs value: {}", outs);
            throw new IllegalArgumentException("Outs must be between 0 and 2");
        }
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setOuts(outs);
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO updateBalls(Long matchId, Integer balls) {
        log.info("Updating balls for match: {} to: {}", matchId, balls);
        if (balls < 0 || balls > 3) {
            log.error("Invalid balls value: {}", balls);
            throw new IllegalArgumentException("Balls must be between 0 and 3");
        }
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setBalls(balls);
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO updateStrikes(Long matchId, Integer strikes) {
        log.info("Updating strikes for match: {} to: {}", matchId, strikes);
        if (strikes < 0 || strikes > 2) {
            log.error("Invalid strikes value: {}", strikes);
            throw new IllegalArgumentException("Strikes must be between 0 and 2");
        }
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setStrikes(strikes);
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO updateBases(Long matchId, Long firstBasePlayerId, Long secondBasePlayerId, Long thirdBasePlayerId) {
        log.info("Updating bases for match: {}", matchId);
        BaseballGameState gameState = getGameStateEntity(matchId);

        if (firstBasePlayerId != null && !playerMatchRepository.existsByIdAndMatchId(firstBasePlayerId, matchId)) {
            throw new IllegalArgumentException("PlayerMatch " + firstBasePlayerId + " does not belong to match " + matchId);
        }
        if (secondBasePlayerId != null && !playerMatchRepository.existsByIdAndMatchId(secondBasePlayerId, matchId)) {
            throw new IllegalArgumentException("PlayerMatch " + secondBasePlayerId + " does not belong to match " + matchId);
        }
        if (thirdBasePlayerId != null && !playerMatchRepository.existsByIdAndMatchId(thirdBasePlayerId, matchId)) {
            throw new IllegalArgumentException("PlayerMatch " + thirdBasePlayerId + " does not belong to match " + matchId);
        }
        gameState.setFirstBasePlayerMatch(firstBasePlayerId != null ? playerMatchRepository.findById(firstBasePlayerId).orElse(null) : null);
        gameState.setSecondBasePlayerMatch(secondBasePlayerId != null ? playerMatchRepository.findById(secondBasePlayerId).orElse(null) : null);
        gameState.setThirdBasePlayerMatch(thirdBasePlayerId != null ? playerMatchRepository.findById(thirdBasePlayerId).orElse(null) : null);

        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO updateScore(Long matchId, Integer homeScore, Integer awayScore) {
        log.info("Updating score for match: {} - Home: {}, Away: {}", matchId, homeScore, awayScore);
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setHomeScore(homeScore);
        gameState.setAwayScore(awayScore);
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO finishGame(Long matchId) {
        log.info("Finishing game for match: {}", matchId);
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setStatus(BaseballGameStatus.FINISHED);
        syncFinishedGameToMatch(gameState);
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    /**
     * Simulates runner advancement through all events to reconstruct base occupancy.
     * Limitations:
     *   - Runner advancement on hits is approximated (conservative: 1B runner advances one base per hit).
     *   - Current batter cannot be reconstructed without the full batting-order lineup, so it is set to null.
     *   - Pitcher is not stored in game state and is therefore not reconstructed here.
     */
    private void reconstructBases(BaseballGameState gameState, List<BaseballPlayEvent> events) {
        PlayerMatch onFirst = null, onSecond = null, onThird = null;
        int currentOuts = 0;

        for (BaseballPlayEvent ev : events) {
            PlayerMatch batter = ev.getBatterPlayerMatch();
            BaseballEventType type = ev.getEventType();
            int outsOnPlay = ev.getOutsOnPlay() != null ? ev.getOutsOnPlay() : 0;

            switch (type) {
                case HOME_RUN -> onFirst = onSecond = onThird = null;
                case TRIPLE -> {
                    onFirst = null;
                    onSecond = null;
                    onThird = batter;
                }
                case DOUBLE -> {
                    // 3B/2B runners score; 1B runner advances to 3B; batter to 2B
                    PlayerMatch old1B = onFirst;
                    onFirst = null;
                    onSecond = batter;
                    onThird = old1B;
                }
                case SINGLE, ERROR -> {
                    // 3B runner scores; 2B→3B; 1B→2B; batter to 1B
                    PlayerMatch old1B = onFirst, old2B = onSecond;
                    onFirst = batter;
                    onSecond = old1B;
                    onThird = old2B;
                }
                case WALK, HIT_BY_PITCH -> {
                    if (onFirst != null) {
                        // Forced advance chain
                        PlayerMatch old1B = onFirst, old2B = onSecond;
                        onFirst = batter;
                        onSecond = old1B;
                        if (old2B != null) {
                            onThird = old2B; // 2B forced to 3B; old 3B scores
                        }
                    } else {
                        onFirst = batter;
                    }
                }
                case SACRIFICE_FLY -> onThird = null; // 3B runner scores on sac fly
                case DOUBLE_PLAY -> {
                    // Batter + 1B runner are typical victims
                    onFirst = null;
                    outsOnPlay = Math.max(outsOnPlay, 2);
                }
                case TRIPLE_PLAY -> {
                    onFirst = null;
                    onSecond = null;
                    outsOnPlay = Math.max(outsOnPlay, 3);
                }
                case CAUGHT_STEALING -> {
                    // Runner is out; most common: 1B attempting steal of 2B
                    if (onFirst != null) onFirst = null;
                    else if (onSecond != null) onSecond = null;
                    outsOnPlay = Math.max(outsOnPlay, 1);
                    currentOuts += outsOnPlay;
                    if (currentOuts >= 3) {
                        currentOuts = 0;
                        onFirst = onSecond = onThird = null;
                    }
                    continue; // not in AT_BAT_ENDING; processed above
                }
                case STOLEN_BASE -> {
                    // Advance the lowest occupied base by one
                    if (onFirst != null) { onSecond = onFirst; onFirst = null; }
                    else if (onSecond != null) { onThird = onSecond; onSecond = null; }
                }
                default -> { /* STRIKEOUT, OUT, SAC_BUNT, PITCHING_CHANGE, etc. — no base change */ }
            }

            if (AT_BAT_ENDING.contains(type)) {
                int recordedOuts = outsOnPlay > 0 ? outsOnPlay
                    : isOutRecordingEvent(type) ? 1 : 0;
                currentOuts += recordedOuts;
                if (currentOuts >= 3) {
                    currentOuts = 0;
                    onFirst = onSecond = onThird = null;
                }
            }
        }

        gameState.setFirstBasePlayerMatch(onFirst);
        gameState.setSecondBasePlayerMatch(onSecond);
        gameState.setThirdBasePlayerMatch(onThird);
        // Next batter cannot be determined from events without full lineup order
        gameState.setCurrentBatterPlayerMatch(null);
    }

    private boolean isOutRecordingEvent(BaseballEventType type) {
        return type == BaseballEventType.STRIKEOUT
            || type == BaseballEventType.OUT
            || type == BaseballEventType.SACRIFICE_FLY
            || type == BaseballEventType.SACRIFICE_BUNT;
    }

    // Default outs recorded by each event type when outsOnPlay is not explicitly set.
    private int defaultOutsForEvent(BaseballEventType type) {
        return switch (type) {
            case STRIKEOUT, OUT, SACRIFICE_FLY, SACRIFICE_BUNT, CAUGHT_STEALING -> 1;
            case DOUBLE_PLAY -> 2;
            case TRIPLE_PLAY -> 3;
            default -> 0; // hits, walks, HBP, stolen base, etc.
        };
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO rebuildGameStateFromEvents(Long matchId) {
        log.info("Rebuilding game state from events for match: {}", matchId);
        BaseballGameState gameState = getGameStateEntity(matchId);
        List<BaseballPlayEvent> events =
            playEventRepository.findAllByMatchIdOrderByCreatedAtAsc(matchId);

        if (events.isEmpty()) {
            return mapper.toDTO(gameState);
        }

        // Reconstruct scores
        int awayScore = 0, homeScore = 0;
        for (BaseballPlayEvent ev : events) {
            int runs = ev.getRunsScored() != null ? ev.getRunsScored() : 0;
            if (runs != 0) {
                if (ev.getInningHalf() == InningHalf.TOP) awayScore += runs;
                else homeScore += runs;
            }
        }
        gameState.setAwayScore(Math.max(0, awayScore));
        gameState.setHomeScore(Math.max(0, homeScore));

        // Reconstruct inning / outs from events (not needed for FINISHED games).
        // Uses defaultOutsForEvent so that CAUGHT_STEALING and other non-AT_BAT events with
        // explicit outs are counted, while hits/walks (which never record outs) are skipped.
        if (gameState.getStatus() != BaseballGameStatus.FINISHED) {
            int currentInning = 1;
            InningHalf currentHalf = InningHalf.TOP;
            int currentOuts = 0;

            for (BaseballPlayEvent ev : events) {
                int outsOnPlay = ev.getOutsOnPlay() != null ? ev.getOutsOnPlay() : defaultOutsForEvent(ev.getEventType());
                if (outsOnPlay <= 0) continue;
                currentOuts += outsOnPlay;
                if (currentOuts >= 3) {
                    currentOuts = 0;
                    if (currentHalf == InningHalf.TOP) {
                        currentHalf = InningHalf.BOTTOM;
                    } else {
                        currentHalf = InningHalf.TOP;
                        currentInning++;
                    }
                }
            }

            int maxInnings = gameState.getTotalInnings() != null ? gameState.getTotalInnings() : 9;
            gameState.setCurrentInning(Math.min(currentInning, maxInnings));
            gameState.setInningHalf(currentHalf);
            gameState.setOuts(Math.min(currentOuts, 2));
            gameState.setBalls(0);
            gameState.setStrikes(0);
            reconstructBases(gameState, events);
        }

        gameState = gameStateRepository.save(gameState);
        log.info("Rebuilt game state for match {}: inning={} {}, outs={}, score={}-{}",
            matchId, gameState.getCurrentInning(), gameState.getInningHalf(),
            gameState.getOuts(), gameState.getAwayScore(), gameState.getHomeScore());
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public void deleteGameState(Long matchId) {
        log.info("Deleting game state for match: {}", matchId);
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameStateRepository.delete(gameState);
        log.info("Game state deleted for match: {}", matchId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "gameState", key = "#matchId")
    public BaseballGameStateDTO updatePitcherTracking(Long matchId,
                                                       Long incomingPitcherPMId,
                                                       Long outgoingPitcherPMId,
                                                       Integer outgoingPitchCount) {
        log.info("Updating pitcher tracking for match: {} | in={} out={} outCount={}",
                matchId, incomingPitcherPMId, outgoingPitcherPMId, outgoingPitchCount);

        final BaseballGameState gameState = getGameStateEntity(matchId);

        // Persist outgoing pitcher's pitch count
        if (outgoingPitcherPMId != null && outgoingPitchCount != null) {
            if (!playerMatchRepository.existsByIdAndMatchId(outgoingPitcherPMId, matchId)) {
                throw new IllegalArgumentException(
                    "Outgoing PlayerMatch " + outgoingPitcherPMId + " does not belong to match " + matchId);
            }
            PitcherPitchCount record = pitcherPitchCountRepository
                    .findByGameStateIdAndPitcherPMId(gameState.getId(), outgoingPitcherPMId)
                    .orElseGet(() -> {
                        PitcherPitchCount newRecord = new PitcherPitchCount();
                        newRecord.setGameState(gameState);
                        PlayerMatch pm = playerMatchRepository.findById(outgoingPitcherPMId)
                                .orElseThrow(() -> new EntityNotFoundException("PlayerMatch not found: " + outgoingPitcherPMId));
                        newRecord.setPitcherPlayerMatch(pm);
                        return newRecord;
                    });
            record.setPitchCount(outgoingPitchCount);
            pitcherPitchCountRepository.save(record);
        }

        // Set incoming pitcher as current and load their saved pitch count
        if (incomingPitcherPMId != null) {
            if (!playerMatchRepository.existsByIdAndMatchId(incomingPitcherPMId, matchId)) {
                throw new IllegalArgumentException(
                    "Incoming PlayerMatch " + incomingPitcherPMId + " does not belong to match " + matchId);
            }
            PlayerMatch incoming = playerMatchRepository.findById(incomingPitcherPMId)
                    .orElseThrow(() -> new EntityNotFoundException("PlayerMatch not found: " + incomingPitcherPMId));
            gameState.setCurrentPitcherPlayerMatch(incoming);

            int restoredCount = pitcherPitchCountRepository
                    .findByGameStateIdAndPitcherPMId(gameState.getId(), incomingPitcherPMId)
                    .map(PitcherPitchCount::getPitchCount)
                    .orElse(0);
            gameState.setPitchCount(restoredCount);
        }

        BaseballGameState saved = gameStateRepository.save(gameState);
        return mapper.toDTO(saved);
    }

    private BaseballGameState getGameStateEntity(Long matchId) {
        return gameStateRepository.findByMatchId(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Game state not found for match: " + matchId));
    }
}
