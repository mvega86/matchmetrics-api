package com.matchmetrics.service.implementation;

import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.MatchMapper;
import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.persistence.entity.Match;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.repository.MatchRepository;
import com.matchmetrics.persistence.repository.PlayerMatchRepository;
import com.matchmetrics.persistence.repository.TeamRepository;
import com.matchmetrics.service.IMatchService;
import com.matchmetrics.service.IPlayerMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MatchService implements IMatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final MatchMapper matchMapper;
    private final IPlayerMatchService playerMatchService;
    private final PlayerMatchRepository playerMatchRepository;

    public MatchService(MatchRepository matchRepository, TeamRepository teamRepository, MatchMapper matchMapper, PlayerMatchRepository playerMatchRepository, IPlayerMatchService playerMatchService, PlayerMatchRepository playerMatchRepository1) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.matchMapper = matchMapper;
        this.playerMatchService = playerMatchService;
        this.playerMatchRepository = playerMatchRepository1;
    }

    @Override
    @Transactional
    public MatchDTO createMatch(MatchDTO matchDTO) {
        log.info("Logger: Creating match...");
        teamRepository.findById(matchDTO.getHomeTeam().getId())
                .orElseThrow(() -> {
                    log.error("Logger: Home team, with id {}, not found", matchDTO.getHomeTeam().getId());
                    return new EntityNotFoundException("Home team not found");
                });
        teamRepository.findById(matchDTO.getAwayTeam().getId())
                .orElseThrow(() -> {
                    log.error("Logger: Away team, with id {}, not found", matchDTO.getAwayTeam().getId());
                    return new EntityNotFoundException("Away team not found");
                });

        if (matchDTO.getHomeTeam().getId().equals(matchDTO.getAwayTeam().getId())) {
            log.error("Logger: The home and away teams cannot be the same.");
            throw new IllegalArgumentException("The home and away teams cannot be the same.");
        }

        try {
            Match match = matchMapper.toEntity(matchDTO);
            match = matchRepository.save(match);
            log.info("Logger: Match created successfully");
            return matchMapper.toDTO(match);
        }catch (Exception e){
            log.error("Logger: Error creating match: {}", e.getMessage());
            throw new RuntimeException("Error creating match.");
        }
    }

    @Override
    public List<MatchDTO> search(String search) {
        if (search != null && search.startsWith("sport:")) {
            String sportStr = search.split(":", 2)[1].trim().toUpperCase();
            try {
                var sportType = com.matchmetrics.domain.enums.SportType.valueOf(sportStr);
                return matchRepository.findBySportTypeOrderByStartFirstTimeAsc(sportType)
                        .stream().map(matchMapper::toDTO).toList();
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
            log.info("Searching matches by team: {}", teamId);
            return matchRepository.findByHomeTeamIdOrAwayTeamIdOrderByStartFirstTimeAsc(teamId, teamId)
                    .stream().map(matchMapper::toDTO).toList();
        }

        if (search != null && search.startsWith("tournament:")) {
            Long tournamentId;
            try {
                tournamentId = Long.parseLong(search.split(":", 2)[1].trim());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID inválido en búsqueda");
            }
            log.info("Searching matches by tournament: {}", tournamentId);
            return matchRepository.findByTournamentIdOrderByStartFirstTimeAsc(tournamentId)
                    .stream().map(matchMapper::toDTO).toList();
        }

        if ("friendly".equalsIgnoreCase(search)) {
            log.info("Searching friendly matches (no tournament)");
            return matchRepository.findByTournamentIsNullOrderByStartFirstTimeAsc()
                    .stream().map(matchMapper::toDTO).toList();
        }

        log.info("Searching all matches...");
        return matchRepository.findAllByOrderByStartFirstTimeAsc()
                .stream().map(matchMapper::toDTO).toList();
    }

    @Override
    public List<MatchDTO> searchByTeam(String search, Long teamId) {
        log.info("Searching matches for authenticated team: {}", teamId);

        return matchRepository
                .findByHomeTeamIdOrAwayTeamIdOrderByStartFirstTimeAsc(teamId, teamId)
                .stream()
                .map(matchMapper::toDTO)
                .toList();
    }

    @Override
    public MatchDTO getMatchById(Long matchId) {
        log.info("Logger: Searching match with id {}...", matchId);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Logger: Match with ID {} not found", matchId);
                    return new EntityNotFoundException("Match not found");
                });
        log.info("Logger: Match found: {}", match.getHomeTeam().getAcronym()+" - "+match.getAwayTeam().getAcronym());
        return matchMapper.toDTO(match);
    }

    @Override
    @Transactional
    public MatchDTO updateMatch(MatchDTO matchDTO) {
        Match existing = matchRepository.findById(matchDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        if (matchDTO.getHomeTeam().getId().equals(matchDTO.getAwayTeam().getId())) {
            throw new IllegalArgumentException("El equipo local y visitante no pueden ser el mismo.");
        }

        // Save the previous value in case comparisons are needed
        LocalDateTime oldStart = existing.getStartFirstTime();
        LocalDateTime newStart = matchDTO.getStartFirstTime();

        List<PlayerMatch> players = existing.getPlayerMatches();

        log.info("Logger: Updating match times for match ID: {}", matchDTO.getId());

        Match match = matchMapper.toEntity(matchDTO);
        match.setPlayerMatches(existing.getPlayerMatches());
        matchRepository.save(match);
        log.info("Logger: Match times updated.");

        if (newStart != null && (oldStart == null || !oldStart.equals(newStart))) {
            log.info("Logger: Updating players match intime field...");
            players.stream()
                    .filter(pm ->
                            (oldStart == null && pm.getInTime() != null) ||
                            (oldStart != null && oldStart.equals(pm.getInTime()))
                    )
                    .forEach(pm -> pm.setInTime(newStart));
            playerMatchRepository.saveAll(players);
            log.info("Logger: Players match updated successfully.");
        }

        return matchMapper.toDTO(match);
    }

    @Override
    public void delete(Long id) {
        log.info("Searching match with id {} to delete...", id);
        Match match = matchRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Team with ID {} not found", id);
                return new EntityNotFoundException("Match with ID " + id + " was not found.");

                });
        /*PlayerMatch playerMatch = playerMatchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PlayerMatch not found"));

        if (!playerMatch.getPlayerStatistics().isEmpty()) {
            throw new IllegalStateException("Cannot delete PlayerMatch with associated statistics");
        }*/
        try {
            matchRepository.deleteById(id);
            log.info("Match {} delete successfully.", match.getHomeTeam().getAcronym() + " VS " + match.getAwayTeam().getAcronym());
        }catch (Exception error){
            log.error("Error to try to remove a Match:", error);
        }
    }
}

