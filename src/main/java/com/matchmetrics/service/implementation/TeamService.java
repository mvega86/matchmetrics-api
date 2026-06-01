package com.matchmetrics.service.implementation;

import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.PlayerMapper;
import com.matchmetrics.mapper.TeamMapper;
import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.persistence.repository.PlayerRepository;
import com.matchmetrics.persistence.repository.TeamRepository;
import com.matchmetrics.service.ITeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// =========================
// SERVICIO TeamService
// =========================
@Slf4j
@Service
public class TeamService implements ITeamService {

    private TeamRepository teamRepository;

    private PlayerRepository playerRepository;

    private TeamMapper teamMapper;

    private PlayerMapper playerMapper;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository, TeamMapper teamMapper, PlayerMapper playerMapper) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.teamMapper = teamMapper;
        this.playerMapper = playerMapper;
    }

    @Override
    public List<TeamDTO> getAll() {
        return teamRepository.findAll().stream().map(teamMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TeamDTO> search(String search) {
        if (search != null && search.startsWith("name:")) {
            String name = search.split(":", 2)[1].trim();

            log.info("Searching teams by name: {}", name);

            return teamRepository.findByNameContainingIgnoreCase(name)
                    .stream()
                    .map(teamMapper::toDTO)
                    .collect(Collectors.toList());
        }

        log.info("Searching all teams...");
        return getAll();
    }

    @Override
    public TeamDTO getById(Long id) {
        log.info("Searching team with id {}...", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Team with ID {} not found", id);
                    return new EntityNotFoundException("Team with ID " + id + " was not found.");
                });
        log.info("Team found: {}...", team.getName());
        return teamRepository.findById(id).map(teamMapper::toDTO).orElse(null);
    }

    @Override
    @Transactional
    public TeamDTO save(TeamDTO teamDTO) {
        log.info("Saving team: {}", teamDTO.getName());

        try {
            // Converting the DTO to an entity with the players found
            Team team = teamMapper.toEntity(teamDTO);
            team = teamRepository.save(team);

            log.info("Team saved successfully");
            return teamMapper.toDTO(team);
        } catch (Exception e){
            log.error("Unexpected error saving team: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving team.");
        }

    }

    @Override
    public void delete(Long id) {
        log.info("Searching team with id {} to delete...", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Team with ID {} not found", id);
                    return new EntityNotFoundException("Team with ID " + id + " was not found.");

                });
        teamRepository.deleteById(id);
        log.info("Team {} delete successfully.", team.getName());
    }

    @Override
    public TeamDTO updateTeam(TeamDTO teamDTO) {
        // Check if the statistic exist
        log.info("Searching team with id {} to update...", teamDTO.getId());
        Optional<Team> optionalTeam = teamRepository.findById(teamDTO.getId());
        if (optionalTeam.isEmpty()) {
            log.error("Team {} not found...", teamDTO.getName());
            throw new EntityNotFoundException("Team '" + teamDTO.getName() + "' not exists.");
        }

        Team team = optionalTeam.get();
        team.setName(teamDTO.getName());
        team.setAcronym(teamDTO.getAcronym());

        return teamMapper.toDTO(teamRepository.save(team));
    }
}
