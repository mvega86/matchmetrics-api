package com.matchmetrics.mapper;

import com.matchmetrics.mapper.dto.BaseballGameStateDTO;
import com.matchmetrics.persistence.entity.BaseballGameState;
import com.matchmetrics.persistence.entity.Match;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.repository.MatchRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import org.springframework.stereotype.Component;

@Component
public class BaseballGameStateMapper {

    private final MatchRepository matchRepository;
    private final PlayerMatchRepository playerMatchRepository;

    public BaseballGameStateMapper(MatchRepository matchRepository, PlayerMatchRepository playerMatchRepository) {
        this.matchRepository = matchRepository;
        this.playerMatchRepository = playerMatchRepository;
    }

    public BaseballGameStateDTO toDTO(BaseballGameState entity) {
        return new BaseballGameStateDTO(
                entity.getId(),
                entity.getMatch().getId(),
                entity.getCurrentInning(),
                entity.getInningHalf(),
                entity.getOuts(),
                entity.getBalls(),
                entity.getStrikes(),
                entity.getHomeScore(),
                entity.getAwayScore(),
                entity.getFirstBasePlayerMatch() != null ? entity.getFirstBasePlayerMatch().getId() : null,
                entity.getSecondBasePlayerMatch() != null ? entity.getSecondBasePlayerMatch().getId() : null,
                entity.getThirdBasePlayerMatch() != null ? entity.getThirdBasePlayerMatch().getId() : null,
                entity.getStatus()
        );
    }

    public BaseballGameState toEntity(BaseballGameStateDTO dto) {
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        PlayerMatch firstBase = dto.getFirstBasePlayerMatchId() != null 
            ? playerMatchRepository.findById(dto.getFirstBasePlayerMatchId()).orElse(null)
            : null;

        PlayerMatch secondBase = dto.getSecondBasePlayerMatchId() != null 
            ? playerMatchRepository.findById(dto.getSecondBasePlayerMatchId()).orElse(null)
            : null;

        PlayerMatch thirdBase = dto.getThirdBasePlayerMatchId() != null 
            ? playerMatchRepository.findById(dto.getThirdBasePlayerMatchId()).orElse(null)
            : null;

        return new BaseballGameState(
                dto.getId(),
                match,
                dto.getCurrentInning(),
                dto.getInningHalf(),
                dto.getOuts(),
                dto.getBalls(),
                dto.getStrikes(),
                dto.getHomeScore(),
                dto.getAwayScore(),
                firstBase,
                secondBase,
                thirdBase,
                dto.getStatus()
        );
    }
}
