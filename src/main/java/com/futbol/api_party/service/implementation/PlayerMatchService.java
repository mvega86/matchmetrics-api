package com.futbol.api_party.service.implementation;

import com.futbol.api_party.exception.EntityNotFoundException;
import com.futbol.api_party.mapper.dto.PlayerMatchDTO;
import com.futbol.api_party.mapper.PlayerMatchMapper;
import com.futbol.api_party.persistence.entity.Match;
import com.futbol.api_party.persistence.entity.Player;
import com.futbol.api_party.persistence.entity.PlayerMatch;
import com.futbol.api_party.persistence.entity.Team;
import com.futbol.api_party.persistence.repository.MatchRepository;
import com.futbol.api_party.persistence.repository.PlayerMatchRepository;
import com.futbol.api_party.persistence.repository.PlayerRepository;
import com.futbol.api_party.persistence.repository.TeamRepository;
import com.futbol.api_party.service.IPlayerMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    @Transactional
    public PlayerMatchDTO assignPlayerToMatch(PlayerMatchDTO playerMatchDTO) {
        log.info("Assigning player to match...");
        matchRepository.findById(playerMatchDTO.getMatch().getId())
                .orElseThrow(() -> {
                    log.error("Match, with id {}, not found.", playerMatchDTO.getMatch().getId());
                    return new EntityNotFoundException("Match not found");
                });
        Player player = playerRepository.findById(playerMatchDTO.getPlayer().getId())
                .orElseThrow(() -> {
                    log.error("Player match, with id {}, not found.", playerMatchDTO.getPlayer().getId());
                    return new EntityNotFoundException("Player not found");
                });

        try {
            PlayerMatch playerMatch = playerMatchMapper.toEntity(playerMatchDTO, player.getTeam());
            playerMatch = playerMatchRepository.save(playerMatch);
            log.info("Player match assigned successfully");
            return playerMatchMapper.toDTO(playerMatch);
        }catch (Exception e){
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
        playerMatchRepository.findById(playerMatchDTO.getId())
                .orElseThrow(() -> {
                    log.error("Logger: PlayerMatch id not found: {}", playerMatchDTO.getId());
                    return new EntityNotFoundException("PlayerMatch not found");
                });

        Team team  = teamRepository.findById(playerMatchDTO.getPlayer().getTeamId())
                .orElseThrow(() -> {
                    log.error("Logger: Team id not found: {}", playerMatchDTO.getPlayer().getTeamId());
                    return new EntityNotFoundException("Team not found");
                });

        log.info("Logger: Updating out time for playerMatch ID: {}", playerMatchDTO.getId());

        PlayerMatch playerMatch = playerMatchMapper.toEntity(playerMatchDTO, team);

        playerMatchRepository.save(playerMatch);
        return playerMatchMapper.toDTO(playerMatch);
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

