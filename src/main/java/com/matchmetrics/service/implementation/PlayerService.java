package com.matchmetrics.service.implementation;

import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.PlayerMapper;
import com.matchmetrics.mapper.dto.PlayerDTO;
import com.matchmetrics.persistence.entity.Player;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.persistence.repository.PlayerRepository;
import com.matchmetrics.persistence.repository.TeamRepository;
import com.matchmetrics.service.IPlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlayerService implements IPlayerService {
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PlayerMapper playerMapper;
    @Override
    @Transactional
    public PlayerDTO save(PlayerDTO playerDTO) {
        /*if (playerDTO.getFullName().equals("error")) {
            throw new RuntimeException("Simulación de error inesperado");
        }*/
        log.info("Saving player: {}", playerDTO.getFullName());

        Team team = null;

        // Checking if the DTO has an associated equipment ID and look for it in the DB
        if (playerDTO.getTeamId() != null) {
            team = teamRepository.findById(playerDTO.getTeamId()).orElseThrow(
                    () -> {
                        log.error("Team with ID {} not found", playerDTO.getTeamId());
                        return new EntityNotFoundException("Team with ID " + playerDTO.getTeamId() + " was not found.");
                    });
        }

        try {
            // Converting the DTO to an entity by passing it the equipment found
            Player player = playerMapper.toEntity(playerDTO, team);
            // Saving the player and return the DTO
            player = playerRepository.save(player);
            log.info("Player saved successfully");
            return playerMapper.toDTO(player);
        } catch (Exception e){
            log.error("Unexpected error saving player: {}", e.getMessage());
            throw new RuntimeException("Error saving player.");
        }

    }

    @Override
    public List<PlayerDTO> searchPlayers(String search) {
        if (search != null && search.startsWith("team:")) {
            Long teamId = Long.parseLong(search.split(":")[1]);
            log.info("Searching players by {}...", search);
            return playerRepository.findByTeamIdOrderByFullNameAsc(teamId)
                    .stream()
                    .map(playerMapper::toDTO)
                    .collect(Collectors.toList());
        }
        log.info("Searching all players...");
        return playerRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());

    }

    @Override
    public PlayerDTO getById(Long id) {
        log.info("Searching player with id {}...", id);
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Player with ID {} not found", id);
                    return new EntityNotFoundException("Player with ID " + id + " was not found.");
                });
        log.info("Player found: {}", player.getFullName());
        return playerRepository.findById(id).map(playerMapper::toDTO).orElse(null);
    }

    @Override
    public void delete(Long id) {
        log.info("Searching player with id {} to delete...", id);
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Player with ID {} not found", id);
                    return new EntityNotFoundException("Player with ID " + id + " was not found.");
                });
        playerRepository.deleteById(id);
        log.info("Player {} delete successfully.", player.getFullName());
    }

    @Override
    public PlayerDTO updateStatistic(PlayerDTO playerDTO) {
        log.info("Updating player: {}", playerDTO.getFullName());

        Team team = null;

        // Checking if the DTO has an associated equipment ID and look for it in the DB
        if (playerDTO.getTeamId() != null) {
            team = teamRepository.findById(playerDTO.getTeamId()).orElseThrow(
                    () -> {
                        log.error("Team with ID {} not found", playerDTO.getTeamId());
                        return new EntityNotFoundException("Team with ID " + playerDTO.getTeamId() + " was not found.");
                    });
        }

        try {
            // Converting the DTO to an entity by passing it the equipment found
            Player player = playerMapper.toEntity(playerDTO, team);
            // Saving the player and return the DTO
            player = playerRepository.save(player);
            log.info("Player updated successfully");
            return playerMapper.toDTO(player);
        } catch (Exception e){
            log.error("Unexpected error updating player: {}", e.getMessage());
            throw new RuntimeException("Error updating player.");
        }
    }

    @Override
    public List<PlayerDTO> searchPlayersByTeam(String search, Long teamId) {
        log.info("Searching players for authenticated team: {}", teamId);

        return playerRepository.findByTeamIdOrderByFullNameAsc(teamId)
                .stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());
    }
}
