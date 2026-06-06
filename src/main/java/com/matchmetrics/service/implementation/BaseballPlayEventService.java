package com.matchmetrics.service.implementation;

import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.BaseballPlayEventMapper;
import com.matchmetrics.mapper.dto.BaseballPlayEventDTO;
import com.matchmetrics.persistence.entity.BaseballPlayEvent;
import com.matchmetrics.persistence.repository.BaseballPlayEventRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import com.matchmetrics.persistence.repository.TeamRepository;
import com.matchmetrics.service.IBaseballPlayEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaseballPlayEventService implements IBaseballPlayEventService {

    private final BaseballPlayEventRepository repository;
    private final BaseballPlayEventMapper mapper;
    private final TeamRepository teamRepository;
    private final PlayerMatchRepository playerMatchRepository;

    public BaseballPlayEventService(
            BaseballPlayEventRepository repository,
            BaseballPlayEventMapper mapper,
            TeamRepository teamRepository,
            PlayerMatchRepository playerMatchRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.teamRepository = teamRepository;
        this.playerMatchRepository = playerMatchRepository;
    }

    @Override
    @Transactional
    public BaseballPlayEventDTO createPlayEvent(BaseballPlayEventDTO dto) {
        log.info("Creating baseball play event for match: {}", dto.getMatchId());

        if (dto.getBatterPlayerMatchId() != null &&
                !playerMatchRepository.existsByIdAndMatchId(dto.getBatterPlayerMatchId(), dto.getMatchId())) {
            throw new IllegalArgumentException(
                    "Batter PlayerMatch " + dto.getBatterPlayerMatchId() + " does not belong to match " + dto.getMatchId());
        }
        if (dto.getPitcherPlayerMatchId() != null &&
                !playerMatchRepository.existsByIdAndMatchId(dto.getPitcherPlayerMatchId(), dto.getMatchId())) {
            throw new IllegalArgumentException(
                    "Pitcher PlayerMatch " + dto.getPitcherPlayerMatchId() + " does not belong to match " + dto.getMatchId());
        }

        BaseballPlayEvent entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toDTO(entity);
    }

    @Override
    public BaseballPlayEventDTO getPlayEventById(Long id) {
        log.info("Retrieving baseball play event by id: {}", id);
        BaseballPlayEvent entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Play event not found: " + id));
        return mapper.toDTO(entity);
    }

    @Override
    public List<BaseballPlayEventDTO> search(String search) {
        if (search != null && search.startsWith("match:")) {
            Long matchId = Long.parseLong(search.split(":", 2)[1].trim());
            log.info("Searching baseball play events for match: {}", matchId);
            return repository.findAllByMatchIdOrderByCreatedAtAsc(matchId)
                    .stream()
                    .map(mapper::toDTO)
                    .collect(Collectors.toList());
        }
        log.info("Getting all baseball play events");
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BaseballPlayEventDTO> searchByTeam(Long teamId) {
        log.info("Searching baseball play events for team: {}", teamId);
        return repository.findByBattingTeamIdOrFieldingTeamIdOrderByCreatedAtAsc(teamId, teamId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BaseballPlayEventDTO updatePlayEvent(Long id, BaseballPlayEventDTO dto) {
        log.info("Updating baseball play event: {}", id);
        BaseballPlayEvent event = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Play event not found: " + id));

        if (dto.getMatchId() != null && !dto.getMatchId().equals(event.getMatch().getId())) {
            throw new IllegalArgumentException("Cannot change match for an existing event.");
        }

        if (dto.getInning() != null) {
            event.setInning(dto.getInning());
        }
        if (dto.getInningHalf() != null) {
            event.setInningHalf(dto.getInningHalf());
        }
        if (dto.getEventType() != null) {
            event.setEventType(dto.getEventType());
        }
        if (dto.getBattingTeamId() != null) {
            event.setBattingTeam(teamRepository.findById(dto.getBattingTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Batting team not found.")));
        }
        if (dto.getFieldingTeamId() != null) {
            event.setFieldingTeam(teamRepository.findById(dto.getFieldingTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Fielding team not found.")));
        }
        if (dto.getBatterPlayerMatchId() != null) {
            if (!playerMatchRepository.existsByIdAndMatchId(dto.getBatterPlayerMatchId(), event.getMatch().getId())) {
                throw new IllegalArgumentException("Batter PlayerMatch does not belong to this match.");
            }
            event.setBatterPlayerMatch(playerMatchRepository.findById(dto.getBatterPlayerMatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Batter player match not found.")));
        }
        if (dto.getPitcherPlayerMatchId() != null) {
            if (!playerMatchRepository.existsByIdAndMatchId(dto.getPitcherPlayerMatchId(), event.getMatch().getId())) {
                throw new IllegalArgumentException("Pitcher PlayerMatch does not belong to this match.");
            }
            event.setPitcherPlayerMatch(playerMatchRepository.findById(dto.getPitcherPlayerMatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Pitcher player match not found.")));
        }
        if (dto.getResult() != null) {
            event.setResult(dto.getResult());
        }
        if (dto.getRunsScored() != null) {
            event.setRunsScored(dto.getRunsScored());
        }
        if (dto.getOutsOnPlay() != null) {
            event.setOutsOnPlay(dto.getOutsOnPlay());
        }
        if (dto.getRbi() != null) {
            event.setRbi(dto.getRbi());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getBallsBefore() != null) {
            event.setBallsBefore(dto.getBallsBefore());
        }
        if (dto.getStrikesBefore() != null) {
            event.setStrikesBefore(dto.getStrikesBefore());
        }
        if (dto.getOutsBefore() != null) {
            event.setOutsBefore(dto.getOutsBefore());
        }
        if (dto.getFirstBaseBefore() != null) {
            event.setFirstBaseBefore(dto.getFirstBaseBefore());
        }
        if (dto.getSecondBaseBefore() != null) {
            event.setSecondBaseBefore(dto.getSecondBaseBefore());
        }
        if (dto.getThirdBaseBefore() != null) {
            event.setThirdBaseBefore(dto.getThirdBaseBefore());
        }

        event = repository.save(event);
        return mapper.toDTO(event);
    }

    @Override
    @Transactional
    public void deletePlayEvent(Long id) {
        log.info("Deleting baseball play event: {}", id);
        BaseballPlayEvent event = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Play event not found: " + id));
        repository.delete(event);
    }

    @Override
    @Transactional
    public void deleteAllByMatchId(Long matchId) {
        log.info("Deleting all baseball play events for match: {}", matchId);
        repository.deleteAllByMatchId(matchId);
    }
}
