package com.futbol.api_party.service.implementation;

import com.futbol.api_party.exception.EntityNotFoundException;
import com.futbol.api_party.mapper.PlayerMatchMapper;
import com.futbol.api_party.mapper.StatisticMapper;
import com.futbol.api_party.mapper.dto.PlayerMatchDTO;
import com.futbol.api_party.mapper.dto.PlayerStatisticDTO;
import com.futbol.api_party.mapper.PlayerStatisticMapper;
import com.futbol.api_party.persistence.entity.Match;
import com.futbol.api_party.persistence.entity.PlayerStatistic;
import com.futbol.api_party.persistence.entity.PlayerMatch;
import com.futbol.api_party.persistence.entity.Statistic;
import com.futbol.api_party.persistence.repository.PlayerStatisticRepository;
import com.futbol.api_party.persistence.repository.PlayerMatchRepository;
import com.futbol.api_party.persistence.repository.StatisticRepository;
import com.futbol.api_party.service.IPlayerStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PlayerStatisticService(PlayerStatisticRepository playerStatisticRepository,
                                  PlayerMatchRepository playerMatchRepository,
                                  PlayerStatisticMapper playerStatisticMapper, PlayerMatchMapper playerMatchMapper, StatisticMapper statisticMapper,
                                  StatisticRepository statisticRepository) {
        this.playerStatisticRepository = playerStatisticRepository;
        this.playerMatchRepository = playerMatchRepository;
        this.playerStatisticMapper = playerStatisticMapper;
        this.playerMatchMapper = playerMatchMapper;
        this.statisticMapper = statisticMapper;
        this.statisticRepository = statisticRepository;
    }

    @Override
    public List<PlayerStatisticDTO> search(String search) {
        if (search != null && search.startsWith("match:")) {
            Long matchId = Long.parseLong(search.split(":")[1]);
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
    @Transactional
    public PlayerStatisticDTO createPlayerStatistic(PlayerStatisticDTO playerStatisticDTO) {
        log.info("Logging: Creating match statistic...");
        PlayerMatch playerMatch = playerMatchRepository.findById(playerStatisticDTO.getPlayerMatch().getId())
                .orElseThrow(() -> {
                    log.error("Logging: PlayerMatch, with id {}, not found.", playerStatisticDTO.getPlayerMatch().getId());
                    return new EntityNotFoundException("Player match not found.");
                });

        Statistic statistic = statisticRepository.findById(playerStatisticDTO.getStatistic().getId())
                .orElseThrow(() -> new EntityNotFoundException("Statistic not found"));



        try {
            PlayerStatistic playerStatistic = playerStatisticMapper.toEntity(playerStatisticDTO, playerMatch, statistic);
            playerStatistic = playerStatisticRepository.save(playerStatistic);
            return playerStatisticMapper.toDTO(playerStatistic);
        }catch (Exception e){
            log.error("Logging: Error creating match statistic: {}", e.getMessage());
            throw new RuntimeException("Error creating match statistic.");
        }
    }

    @Override
    public List<PlayerStatisticDTO> getStatisticsByPlayerMatch(Long playerMatchId) {
        log.info("Logging: Searching playermatch with id {}...", playerMatchId);
        return playerStatisticRepository.findByPlayerMatchId(playerMatchId).stream()
                .map(playerStatisticMapper::toDTO).toList();
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
        log.info("Logging: Updating...");
        PlayerStatistic updated = playerStatisticMapper.toEntity(playerStatisticDTO, playerMatch, statistic);
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
        try {
            playerStatisticRepository.deleteById(id);
            log.info("Player statistic with id {} delete successfully.", playerStatistic.getId());
        }catch (Exception error){
            log.error("Error to try to remove a player statistic:", error);
        }
    }
}

