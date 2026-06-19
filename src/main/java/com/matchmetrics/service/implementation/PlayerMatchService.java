package com.matchmetrics.service.implementation;

import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.dto.PlayerMatchDTO;
import com.matchmetrics.mapper.PlayerMatchMapper;
import com.matchmetrics.persistence.entity.Player;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.persistence.repository.MatchRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import com.matchmetrics.persistence.repository.PlayerRepository;
import com.matchmetrics.persistence.repository.TeamRepository;
import com.matchmetrics.service.IPlayerMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlayerMatchService implements IPlayerMatchService {

    private final PlayerMatchRepository playerMatchRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final PlayerMatchMapper playerMatchMapper;

    public PlayerMatchService(PlayerMatchRepository playerMatchRepository, MatchRepository matchRepository,
                              TeamRepository teamRepository, PlayerRepository playerRepository, PlayerMatchMapper playerMatchMapper) {
        this.playerMatchRepository = playerMatchRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.playerMatchMapper = playerMatchMapper;
    }

    @Override
    public List<PlayerMatchDTO> search(String search) {
        if (search != null && search.startsWith("match:")) {
            Long matchId = Long.parseLong(search.split(":")[1]);
            log.info("Searching players by {}...", search);
            return playerMatchRepository.findByMatchId(matchId)
                    .stream()
                    .map(playerMatchMapper::toDTO)
                    .collect(Collectors.toList());
        }
        log.info("Searching all players...");
        return playerMatchRepository.findAll()
                .stream()
                .map(playerMatchMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerMatchDTO> searchByTeam(String search, Long teamId) {
        if (search != null && search.startsWith("match:")) {
            Long matchId = Long.parseLong(search.split(":", 2)[1]);

            log.info("Searching player matches by match {} and authenticated team {}", matchId, teamId);

            return playerMatchRepository.findByMatchIdAndPlayerTeamId(matchId, teamId)
                    .stream()
                    .map(playerMatchMapper::toDTO)
                    .collect(Collectors.toList());
        }

        log.info("Searching player matches for authenticated team: {}", teamId);

        return playerMatchRepository.findByPlayerTeamId(teamId)
                .stream()
                .map(playerMatchMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlayerMatchDTO save(PlayerMatchDTO playerMatchDTO) {
        log.info("Assigning player to match...");

        Long matchId = playerMatchDTO.getMatch().getId();
        Long playerId = playerMatchDTO.getPlayer().getId();

        matchRepository.findById(matchId).orElseThrow(() -> {
            log.error("Match with id {} not found.", matchId);
            return new EntityNotFoundException("Match not found");
        });

        Player player = playerRepository.findById(playerId).orElseThrow(() -> {
            log.error("Player with id {} not found.", playerId);
            return new EntityNotFoundException("Player not found");
        });

        Optional<PlayerMatch> existing = playerMatchRepository.findByMatchIdAndPlayerId(matchId, playerId);
        if (existing.isPresent()) {
            log.error("Player {} is already assigned to match {}", playerId, matchId);
            throw new RuntimeException("Player is already assigned to this match.");
        }

        try {
            PlayerMatch playerMatch = playerMatchMapper.toEntity(playerMatchDTO, player.getTeam());
            playerMatch = playerMatchRepository.save(playerMatch);
            log.info("Player match created successfully.");
            return playerMatchMapper.toDTO(playerMatch);
        } catch (Exception e) {
            log.error("Error assigning player to match: {}", e.getMessage());
            throw new RuntimeException("Error assigning player to match.");
        }
    }


    @Override
    public PlayerMatchDTO getById(Long id) {
        log.info("Searching player match with id {}...", id);
        PlayerMatch playerMatch = playerMatchRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("PlayerMatch, with id {}, not found.", id);
                    return new EntityNotFoundException("PlayerMatch not found");
                });
        return playerMatchMapper.toDTO(playerMatch);
    }

    @Override
    @Transactional
    public PlayerMatchDTO updatePlayerMatch(PlayerMatchDTO playerMatchDTO) {
        PlayerMatch existing = playerMatchRepository.findById(playerMatchDTO.getId())
                .orElseThrow(() -> {
                    log.error("Logger: PlayerMatch id not found: {}", playerMatchDTO.getId());
                    return new EntityNotFoundException("PlayerMatch not found");
                });

        boolean positionOnly = Boolean.TRUE.equals(playerMatchDTO.getFieldPositionOnly());
        log.info("Logger: Updating playerMatch ID: {} | DTO fieldPosition='{}' battingOrder={} fieldPositionOnly={}",
            playerMatchDTO.getId(), playerMatchDTO.getFieldPosition(), playerMatchDTO.getBattingOrder(), positionOnly);

        existing.setFieldPosition(playerMatchDTO.getFieldPosition());
        if (!positionOnly) {
            existing.setBattingOrder(playerMatchDTO.getBattingOrder());
            existing.setInTime(playerMatchDTO.getInTime());
            existing.setOutTime(playerMatchDTO.getOutTime());
        }

        playerMatchRepository.save(existing);
        log.info("Logger: After save, ID: {} | fieldPosition='{}' battingOrder={}",
            existing.getId(), existing.getFieldPosition(), existing.getBattingOrder());
        return playerMatchMapper.toDTO(existing);
    }

    public void delete(Long id) {
        log.info("Searching player match with id {} to delete...", id);
        PlayerMatch playerMatch = playerMatchRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Team with ID {} not found", id);
                    return new EntityNotFoundException("Team with ID " + id + " was not found.");

                });
        playerMatchRepository.deleteById(id);
        log.info("Match {} delete successfully.", playerMatch.getId());
    }
}

