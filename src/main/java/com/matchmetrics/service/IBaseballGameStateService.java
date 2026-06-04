package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.BaseballGameStateDTO;

public interface IBaseballGameStateService {
    BaseballGameStateDTO createGameState(BaseballGameStateDTO dto);
    BaseballGameStateDTO getGameStateByMatchId(Long matchId);
    BaseballGameStateDTO updateGameState(Long matchId, BaseballGameStateDTO dto);
    BaseballGameStateDTO updateInning(Long matchId, Integer inning);
    BaseballGameStateDTO updateInningHalf(Long matchId, String inningHalf);
    BaseballGameStateDTO updateOuts(Long matchId, Integer outs);
    BaseballGameStateDTO updateBalls(Long matchId, Integer balls);
    BaseballGameStateDTO updateStrikes(Long matchId, Integer strikes);
    BaseballGameStateDTO updateBases(Long matchId, Long firstBasePlayerId, Long secondBasePlayerId, Long thirdBasePlayerId);
    BaseballGameStateDTO updateScore(Long matchId, Integer homeScore, Integer awayScore);
    BaseballGameStateDTO finishGame(Long matchId);
    void deleteGameState(Long matchId);
}
