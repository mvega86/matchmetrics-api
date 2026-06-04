package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.BaseballGameStatus;
import com.matchmetrics.domain.enums.InningHalf;
import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.BaseballGameStateMapper;
import com.matchmetrics.mapper.dto.BaseballGameStateDTO;
import com.matchmetrics.persistence.entity.BaseballGameState;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.repository.BaseballGameStateRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import com.matchmetrics.service.IBaseballGameStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BaseballGameStateService implements IBaseballGameStateService {

    private final BaseballGameStateRepository gameStateRepository;
    private final PlayerMatchRepository playerMatchRepository;
    private final BaseballGameStateMapper mapper;

    public BaseballGameStateService(
            BaseballGameStateRepository gameStateRepository,
            PlayerMatchRepository playerMatchRepository,
            BaseballGameStateMapper mapper
    ) {
        this.gameStateRepository = gameStateRepository;
        this.playerMatchRepository = playerMatchRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public BaseballGameStateDTO createGameState(BaseballGameStateDTO dto) {
        log.info("Creating baseball game state for match: {}", dto.getMatchId());

        if (gameStateRepository.existsByMatchId(dto.getMatchId())) {
            log.error("Game state already exists for match: {}", dto.getMatchId());
            throw new IllegalArgumentException("Game state already exists for this match");
        }

        try {
            BaseballGameState gameState = mapper.toEntity(dto);
            gameState.setStatus(BaseballGameStatus.NOT_STARTED);
            gameState = gameStateRepository.save(gameState);
            log.info("Baseball game state created successfully for match: {}", dto.getMatchId());
            return mapper.toDTO(gameState);
        } catch (Exception e) {
            log.error("Error creating game state: {}", e.getMessage());
            throw new RuntimeException("Error creating game state");
        }
    }

    @Override
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
    public BaseballGameStateDTO updateInning(Long matchId, Integer inning) {
        log.info("Updating inning for match: {} to inning: {}", matchId, inning);
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setCurrentInning(inning);
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    public BaseballGameStateDTO updateInningHalf(Long matchId, String inningHalf) {
        log.info("Updating inning half for match: {} to: {}", matchId, inningHalf);
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setInningHalf(InningHalf.valueOf(inningHalf.toUpperCase()));
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    public BaseballGameStateDTO updateOuts(Long matchId, Integer outs) {
        log.info("Updating outs for match: {} to: {}", matchId, outs);
        if (outs < 0 || outs > 3) {
            log.error("Invalid outs value: {}", outs);
            throw new IllegalArgumentException("Outs must be between 0 and 3");
        }
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setOuts(outs);
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
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
    public BaseballGameStateDTO updateBases(Long matchId, Long firstBasePlayerId, Long secondBasePlayerId, Long thirdBasePlayerId) {
        log.info("Updating bases for match: {}", matchId);
        BaseballGameState gameState = getGameStateEntity(matchId);

        gameState.setFirstBasePlayerMatch(firstBasePlayerId != null ? playerMatchRepository.findById(firstBasePlayerId).orElse(null) : null);
        gameState.setSecondBasePlayerMatch(secondBasePlayerId != null ? playerMatchRepository.findById(secondBasePlayerId).orElse(null) : null);
        gameState.setThirdBasePlayerMatch(thirdBasePlayerId != null ? playerMatchRepository.findById(thirdBasePlayerId).orElse(null) : null);

        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
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
    public BaseballGameStateDTO finishGame(Long matchId) {
        log.info("Finishing game for match: {}", matchId);
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameState.setStatus(BaseballGameStatus.FINISHED);
        gameState = gameStateRepository.save(gameState);
        return mapper.toDTO(gameState);
    }

    @Override
    @Transactional
    public void deleteGameState(Long matchId) {
        log.info("Deleting game state for match: {}", matchId);
        BaseballGameState gameState = getGameStateEntity(matchId);
        gameStateRepository.delete(gameState);
        log.info("Game state deleted for match: {}", matchId);
    }

    private BaseballGameState getGameStateEntity(Long matchId) {
        return gameStateRepository.findByMatchId(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Game state not found for match: " + matchId));
    }
}
