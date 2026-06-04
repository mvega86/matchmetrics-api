package com.matchmetrics.mapper;

import com.matchmetrics.mapper.dto.BaseballPlayEventDTO;
import com.matchmetrics.persistence.entity.BaseballPlayEvent;
import com.matchmetrics.persistence.entity.Match;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.persistence.repository.MatchRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import com.matchmetrics.persistence.repository.TeamRepository;
import org.springframework.stereotype.Component;

@Component
public class BaseballPlayEventMapper {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerMatchRepository playerMatchRepository;

    public BaseballPlayEventMapper(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            PlayerMatchRepository playerMatchRepository
    ) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerMatchRepository = playerMatchRepository;
    }

    public BaseballPlayEventDTO toDTO(BaseballPlayEvent entity) {
        return new BaseballPlayEventDTO(
                entity.getId(),
                entity.getMatch() != null ? entity.getMatch().getId() : null,
                entity.getInning(),
                entity.getInningHalf(),
                entity.getEventType(),
                entity.getBattingTeam() != null ? entity.getBattingTeam().getId() : null,
                entity.getFieldingTeam() != null ? entity.getFieldingTeam().getId() : null,
                entity.getBatterPlayerMatch() != null ? entity.getBatterPlayerMatch().getId() : null,
                entity.getPitcherPlayerMatch() != null ? entity.getPitcherPlayerMatch().getId() : null,
                entity.getResult(),
                entity.getRunsScored(),
                entity.getOutsOnPlay(),
                entity.getRbi(),
                entity.getDescription(),
                entity.getBallsBefore(),
                entity.getStrikesBefore(),
                entity.getOutsBefore(),
                entity.getFirstBaseBefore(),
                entity.getSecondBaseBefore(),
                entity.getThirdBaseBefore(),
                entity.getCreatedAt()
        );
    }

    public BaseballPlayEvent toEntity(BaseballPlayEventDTO dto) {
        BaseballPlayEvent entity = new BaseballPlayEvent();
        entity.setId(dto.getId());

        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found."));
        entity.setMatch(match);

        Team battingTeam = teamRepository.findById(dto.getBattingTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Batting team not found."));
        entity.setBattingTeam(battingTeam);

        Team fieldingTeam = teamRepository.findById(dto.getFieldingTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Fielding team not found."));
        entity.setFieldingTeam(fieldingTeam);

        entity.setInning(dto.getInning());
        entity.setInningHalf(dto.getInningHalf());
        entity.setEventType(dto.getEventType());
        entity.setResult(dto.getResult());
        entity.setRunsScored(dto.getRunsScored());
        entity.setOutsOnPlay(dto.getOutsOnPlay());
        entity.setRbi(dto.getRbi());
        entity.setDescription(dto.getDescription());
        entity.setBallsBefore(dto.getBallsBefore());
        entity.setStrikesBefore(dto.getStrikesBefore());
        entity.setOutsBefore(dto.getOutsBefore());
        entity.setFirstBaseBefore(dto.getFirstBaseBefore());
        entity.setSecondBaseBefore(dto.getSecondBaseBefore());
        entity.setThirdBaseBefore(dto.getThirdBaseBefore());

        if (dto.getBatterPlayerMatchId() != null) {
            PlayerMatch batterPlayerMatch = playerMatchRepository.findById(dto.getBatterPlayerMatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Batter player match not found."));
            entity.setBatterPlayerMatch(batterPlayerMatch);
        }

        if (dto.getPitcherPlayerMatchId() != null) {
            PlayerMatch pitcherPlayerMatch = playerMatchRepository.findById(dto.getPitcherPlayerMatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Pitcher player match not found."));
            entity.setPitcherPlayerMatch(pitcherPlayerMatch);
        }

        return entity;
    }
}
