package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.PlayerMatchMapper;
import com.matchmetrics.mapper.StatisticMapper;
import com.matchmetrics.mapper.dto.PlayerStatisticDTO;
import com.matchmetrics.mapper.PlayerStatisticMapper;
import com.matchmetrics.persistence.repository.FieldZoneRepository;
import com.matchmetrics.persistence.repository.PlayerStatisticRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import com.matchmetrics.persistence.repository.StatisticRepository;
import com.matchmetrics.service.IPlayerStatisticService;
import com.matchmetrics.persistence.entity.FieldZone;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.entity.PlayerStatistic;
import com.matchmetrics.persistence.entity.Statistic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlayerStatisticService implements IPlayerStatisticService {

    private final PlayerStatisticRepository playerStatisticRepository;
    private final PlayerMatchRepository playerMatchRepository;
    private final PlayerStatisticMapper playerStatisticMapper;
    private final PlayerMatchMapper playerMatchMapper;
    private final StatisticMapper statisticMapper;
    private final StatisticRepository statisticRepository;
    private final FieldZoneRepository fieldZoneRepository;

    public PlayerStatisticService(PlayerStatisticRepository playerStatisticRepository,
                                  PlayerMatchRepository playerMatchRepository,
                                  PlayerStatisticMapper playerStatisticMapper, PlayerMatchMapper playerMatchMapper, StatisticMapper statisticMapper,
                                  StatisticRepository statisticRepository, FieldZoneRepository fieldZoneRepository) {
        this.playerStatisticRepository = playerStatisticRepository;
        this.playerMatchRepository = playerMatchRepository;
        this.playerStatisticMapper = playerStatisticMapper;
        this.playerMatchMapper = playerMatchMapper;
        this.statisticMapper = statisticMapper;
        this.statisticRepository = statisticRepository;
        this.fieldZoneRepository = fieldZoneRepository;
    }

    @Override
    public List<PlayerStatisticDTO> search(String search) {
        if (search != null && search.startsWith("match:")) {
            Long matchId;
            try {
                matchId = Long.parseLong(search.split(":", 2)[1].trim());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID inválido en búsqueda");
            }
            log.info("Searching players by {}...", search);
            return playerStatisticRepository.findByPlayerMatch_Match_IdOrderByCreatedAtDesc(matchId)
                    .stream()
                    .map(playerStatisticMapper::toDTO)
                    .collect(Collectors.toList());
        }
        log.info("Searching all players...");
        return playerStatisticRepository.findAll()
                .stream()
                .map(playerStatisticMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerStatisticDTO> searchByTeam(String search, Long teamId) {
        if (search != null && search.startsWith("match:")) {
            Long matchId;
            try {
                matchId = Long.parseLong(search.split(":", 2)[1].trim());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID inválido en búsqueda");
            }

            log.info("Searching player statistics by match {} and authenticated team {}", matchId, teamId);

            return playerStatisticRepository
                    .findByPlayerMatchMatchIdAndPlayerMatchPlayerTeamIdOrderByCreatedAtDesc(matchId, teamId)
                    .stream()
                    .map(playerStatisticMapper::toDTO)
                    .collect(Collectors.toList());
        }

        log.info("Searching player statistics for authenticated team: {}", teamId);

        return playerStatisticRepository
                .findByPlayerMatchPlayerTeamIdOrderByCreatedAtDesc(teamId)
                .stream()
                .map(playerStatisticMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlayerStatisticDTO createPlayerStatistic(PlayerStatisticDTO playerStatisticDTO) {
        log.info("Logging: Creating match statistic...");
        PlayerMatch playerMatch = playerMatchRepository.findById(playerStatisticDTO.getPlayerMatch().getId())
                .orElseThrow(() -> {
                    log.error("Logging: PlayerMatch, with id {}, not found.", playerStatisticDTO.getPlayerMatch().getId());
                    return new EntityNotFoundException("Player match not found.");
                });

        if (playerMatch.getMatch().getState() == MatchState.FINISHED) {
            throw new IllegalStateException("Cannot register statistics on a finished match.");
        }

        Statistic statistic = statisticRepository.findById(playerStatisticDTO.getStatistic().getId())
                .orElseThrow(() -> new EntityNotFoundException("Statistic not found"));

        log.info("Logging: Field zone searching...");
        FieldZone zone = null;
        if (playerStatisticDTO.getPositionX() != null && playerStatisticDTO.getPositionY() != null) {
            zone = fieldZoneRepository.findByPosition(
                    playerStatisticDTO.getPositionX(), playerStatisticDTO.getPositionY()
            );
        }
        log.info("Logging: Done.");

        try {
            PlayerStatistic playerStatistic = playerStatisticMapper.toEntity(playerStatisticDTO, playerMatch, statistic, zone);
            playerStatistic = playerStatisticRepository.save(playerStatistic);
            return playerStatisticMapper.toDTO(playerStatistic);
        }catch (Exception e){
            log.error("Logging: Error creating match statistic: {}", e.getMessage());
            throw new RuntimeException("Error creating match statistic.");
        }
    }

    @Override
    public PlayerStatisticDTO getById(Long id) {
        PlayerStatistic playerStatistic = playerStatisticRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return playerStatisticMapper.toDTO(playerStatistic);
    }

    @Override
    @Transactional
    public PlayerStatisticDTO update(PlayerStatisticDTO playerStatisticDTO) {
        log.info("Logging: Player statistic with id {}, searching...", playerStatisticDTO.getId());
        playerStatisticRepository.findById(playerStatisticDTO.getId())
                .orElseThrow(() -> {
                    log.error("Logging: Player statistic with id {} not found.", playerStatisticDTO.getId());
                    return new EntityNotFoundException("PlayerStatistic not found");
                });
        log.info("Logging: Done.");
        log.info("Logging: Player match with id {}, searching...", playerStatisticDTO.getPlayerMatch().getId());
        PlayerMatch playerMatch = playerMatchRepository.findById(playerStatisticDTO.getPlayerMatch().getId())
                .orElseThrow(() -> {
                    log.error("Logging: Player match with id {} not found.", playerStatisticDTO.getPlayerMatch().getId());
                    return new EntityNotFoundException("PlayerMatch not found");
                });
        log.info("Logging: Done.");
        log.info("Logging: Statistic with id {}, searching...", playerStatisticDTO.getStatistic().getId());
        Statistic statistic = statisticRepository.findById(playerStatisticDTO.getStatistic().getId())
                .orElseThrow(() -> {
                    log.error("Logging: Statistic with id {} not found.", playerStatisticDTO.getStatistic().getId());
                    return new EntityNotFoundException("Statistic not found");
                });
        log.info("Logging: Done.");
        log.info("Logging: Field zone searching...");
        FieldZone zone = null;
        if (playerStatisticDTO.getPositionX() != null && playerStatisticDTO.getPositionY() != null) {
            zone = fieldZoneRepository.findByPosition(
                    playerStatisticDTO.getPositionX(), playerStatisticDTO.getPositionY()
            );
        }
        log.info("Logging: Done.");
        log.info("Logging: Updating...");
        PlayerStatistic updated = playerStatisticMapper.toEntity(playerStatisticDTO, playerMatch, statistic, zone);
        PlayerStatistic saved = playerStatisticRepository.save(updated);
        log.info("Logging: Done.");
        return playerStatisticMapper.toDTO(saved);
    }

    @Override
    public void delete(Long id) {
        log.info("Searching match with id {} to delete...", id);
        PlayerStatistic playerStatistic = playerStatisticRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Player statistic with ID {} not found", id);
                    return new EntityNotFoundException("Player statistic with ID " + id + " was not found.");

                });
        playerStatisticRepository.deleteById(id);
        log.info("Player statistic with id {} deleted successfully.", playerStatistic.getId());
    }
}

