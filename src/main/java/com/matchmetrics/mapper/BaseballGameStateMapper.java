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
        BaseballGameStateDTO dto = new BaseballGameStateDTO();
        dto.setId(entity.getId());
        dto.setMatchId(entity.getMatch().getId());
        dto.setCurrentInning(entity.getCurrentInning());
        dto.setInningHalf(entity.getInningHalf());
        dto.setOuts(entity.getOuts());
        dto.setBalls(entity.getBalls());
        dto.setStrikes(entity.getStrikes());
        dto.setHomeScore(entity.getHomeScore());
        dto.setAwayScore(entity.getAwayScore());
        dto.setFirstBasePlayerMatchId(entity.getFirstBasePlayerMatch() != null ? entity.getFirstBasePlayerMatch().getId() : null);
        dto.setSecondBasePlayerMatchId(entity.getSecondBasePlayerMatch() != null ? entity.getSecondBasePlayerMatch().getId() : null);
        dto.setThirdBasePlayerMatchId(entity.getThirdBasePlayerMatch() != null ? entity.getThirdBasePlayerMatch().getId() : null);
        dto.setCurrentBatterPlayerMatchId(entity.getCurrentBatterPlayerMatch() != null ? entity.getCurrentBatterPlayerMatch().getId() : null);
        dto.setPitchCount(entity.getPitchCount() != null ? entity.getPitchCount() : 0);
        dto.setStatus(entity.getStatus());
        dto.setTotalInnings(entity.getTotalInnings());
        return dto;
    }

    public BaseballGameState toEntity(BaseballGameStateDTO dto) {
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        PlayerMatch firstBase = dto.getFirstBasePlayerMatchId() != null
            ? playerMatchRepository.findById(dto.getFirstBasePlayerMatchId()).orElse(null) : null;
        PlayerMatch secondBase = dto.getSecondBasePlayerMatchId() != null
            ? playerMatchRepository.findById(dto.getSecondBasePlayerMatchId()).orElse(null) : null;
        PlayerMatch thirdBase = dto.getThirdBasePlayerMatchId() != null
            ? playerMatchRepository.findById(dto.getThirdBasePlayerMatchId()).orElse(null) : null;

        BaseballGameState entity = new BaseballGameState();
        entity.setId(dto.getId());
        entity.setMatch(match);
        entity.setCurrentInning(dto.getCurrentInning());
        entity.setInningHalf(dto.getInningHalf());
        entity.setOuts(dto.getOuts() != null ? dto.getOuts() : 0);
        entity.setBalls(dto.getBalls() != null ? dto.getBalls() : 0);
        entity.setStrikes(dto.getStrikes() != null ? dto.getStrikes() : 0);
        entity.setHomeScore(dto.getHomeScore() != null ? dto.getHomeScore() : 0);
        entity.setAwayScore(dto.getAwayScore() != null ? dto.getAwayScore() : 0);
        entity.setFirstBasePlayerMatch(firstBase);
        entity.setSecondBasePlayerMatch(secondBase);
        entity.setThirdBasePlayerMatch(thirdBase);
        entity.setStatus(dto.getStatus());
        // totalInnings is set by the service based on match sport type
        return entity;
    }
}
