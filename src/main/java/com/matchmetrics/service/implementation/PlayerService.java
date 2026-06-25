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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        log.info("Saving player: {}", playerDTO.getFullName());

        Team team = null;
        if (playerDTO.getTeamId() != null) {
            team = teamRepository.findById(playerDTO.getTeamId()).orElseThrow(
                () -> {
                    log.error("Team with ID {} not found", playerDTO.getTeamId());
                    return new EntityNotFoundException("Team with ID " + playerDTO.getTeamId() + " was not found.");
                });
        }

        try {
            Player player = playerMapper.toEntity(playerDTO, team);
            if (team != null) {
                player.getTeams().add(team);
            }
            player = playerRepository.save(player);
            log.info("Player saved successfully");
            return playerMapper.toDTO(player);
        } catch (Exception e) {
            log.error("Unexpected error saving player: {}", e.getMessage());
            throw new RuntimeException("Error saving player.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerDTO> searchPlayers(String search) {
        if (search != null && search.startsWith("sport:")) {
            String sportStr = search.split(":", 2)[1].trim().toUpperCase();
            try {
                var sportType = com.matchmetrics.domain.enums.SportType.valueOf(sportStr);
                return playerRepository.findByTeamsSportTypeOrderByFullNameAsc(sportType)
                        .stream().map(playerMapper::toDTO).collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        if (search != null && search.startsWith("team:")) {
            Long teamId;
            try {
                teamId = Long.parseLong(search.split(":", 2)[1].trim());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID inválido en búsqueda");
            }
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
    @Transactional(readOnly = true)
    public PlayerDTO getById(Long id) {
        log.info("Searching player with id {}...", id);
        return playerRepository.findById(id)
                .map(playerMapper::toDTO)
                .orElseThrow(() -> {
                    log.error("Player with ID {} not found", id);
                    return new EntityNotFoundException("Player with ID " + id + " was not found.");
                });
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
    @Transactional
    public PlayerDTO updateStatistic(PlayerDTO playerDTO) {
        log.info("Updating player: {}", playerDTO.getFullName());

        Player player = playerRepository.findById(playerDTO.getId()).orElseThrow(() -> {
            log.error("Player with ID {} not found", playerDTO.getId());
            return new EntityNotFoundException("Player with ID " + playerDTO.getId() + " was not found.");
        });

        player.setFullName(playerDTO.getFullName());
        player.setJerseyName(playerDTO.getJerseyName());
        player.setJerseyNumber(playerDTO.getJerseyNumber());
        player.setBirthDate(playerDTO.getBirthDate());
        player.setPhotoUrl(playerDTO.getPhotoUrl());
        player.setFieldPosition(playerDTO.getFieldPosition());

        if (playerDTO.getTeamId() != null) {
            Team newTeam = teamRepository.findById(playerDTO.getTeamId()).orElseThrow(() -> {
                log.error("Team with ID {} not found", playerDTO.getTeamId());
                return new EntityNotFoundException("Team with ID " + playerDTO.getTeamId() + " was not found.");
            });
            // Replace team for this sport, keep teams for other sports
            player.getTeams().removeIf(t -> t.getSportType() == newTeam.getSportType());
            player.getTeams().add(newTeam);
            // Update primary team when same sport or no primary team yet
            if (player.getTeam() == null || player.getTeam().getSportType() == newTeam.getSportType()) {
                player.setTeam(newTeam);
            }
        }

        try {
            player = playerRepository.save(player);
            log.info("Player updated successfully");
            return playerMapper.toDTO(player);
        } catch (Exception e) {
            log.error("Unexpected error updating player: {}", e.getMessage());
            throw new RuntimeException("Error updating player.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerDTO> searchPlayersByTeam(String search, Long teamId) {
        log.info("Searching players for authenticated team: {}", teamId);
        return playerRepository.findByTeamIdOrderByFullNameAsc(teamId)
                .stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());
    }
}
