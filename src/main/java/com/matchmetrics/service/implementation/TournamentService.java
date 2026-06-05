package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.MatchPhase;
import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.exception.EntityNotFoundException;
import com.matchmetrics.mapper.MatchMapper;
import com.matchmetrics.mapper.TeamMapper;
import com.matchmetrics.mapper.TournamentMapper;
import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.mapper.dto.TournamentDTO;
import com.matchmetrics.persistence.entity.Match;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.persistence.entity.Tournament;
import com.matchmetrics.persistence.repository.MatchRepository;
import com.matchmetrics.persistence.repository.TeamRepository;
import com.matchmetrics.persistence.repository.TournamentRepository;
import com.matchmetrics.service.ITournamentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TournamentService implements ITournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentMapper tournamentMapper;
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;

    public TournamentService(
            TournamentRepository tournamentRepository,
            TournamentMapper tournamentMapper,
            TeamRepository teamRepository,
            TeamMapper teamMapper,
            MatchRepository matchRepository,
            MatchMapper matchMapper
    ) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentMapper = tournamentMapper;
        this.teamRepository = teamRepository;
        this.teamMapper = teamMapper;
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
    }

    @Override
    public List<TournamentDTO> search(String search) {
        if (search == null || search.isBlank()) {
            return tournamentRepository.findAllByOrderByStartDateDesc()
                    .stream().map(tournamentMapper::toDTO).toList();
        }
        if (search.startsWith("sport:")) {
            String sport = search.split(":", 2)[1].trim().toUpperCase();
            try {
                var sportType = com.matchmetrics.domain.enums.SportType.valueOf(sport);
                return tournamentRepository.findBySportTypeOrderByStartDateDesc(sportType)
                        .stream().map(tournamentMapper::toDTO).toList();
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        if (search.startsWith("status:")) {
            String statusStr = search.split(":", 2)[1].trim().toUpperCase();
            try {
                var status = com.matchmetrics.domain.enums.TournamentStatus.valueOf(statusStr);
                return tournamentRepository.findByStatusOrderByStartDateDesc(status)
                        .stream().map(tournamentMapper::toDTO).toList();
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        return tournamentRepository.findByNameContainingIgnoreCaseOrderByStartDateDesc(search)
                .stream().map(tournamentMapper::toDTO).toList();
    }

    @Override
    public TournamentDTO getById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found: " + id));
        return tournamentMapper.toDTO(tournament);
    }

    @Override
    @Transactional
    public TournamentDTO create(TournamentDTO dto) {
        log.info("Creating tournament: {}", dto.getName());
        Tournament tournament = tournamentMapper.toEntity(dto);
        tournament = tournamentRepository.save(tournament);
        return tournamentMapper.toDTO(tournament);
    }

    @Override
    @Transactional
    public TournamentDTO update(Long id, TournamentDTO dto) {
        Tournament existing = tournamentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found: " + id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setSportType(dto.getSportType());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setOrganizer(dto.getOrganizer());
        existing.setLocation(dto.getLocation());
        existing.setCountry(dto.getCountry());
        existing.setCategory(dto.getCategory());
        existing = tournamentRepository.save(existing);
        return tournamentMapper.toDTO(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!tournamentRepository.existsById(id)) {
            throw new EntityNotFoundException("Tournament not found: " + id);
        }
        tournamentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TournamentDTO addTeam(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found: " + tournamentId));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + teamId));

        boolean alreadyIn = tournament.getTeams().stream().anyMatch(t -> t.getId().equals(teamId));
        if (!alreadyIn) {
            tournament.getTeams().add(team);
            tournamentRepository.save(tournament);
        }
        return tournamentMapper.toDTO(tournament);
    }

    @Override
    @Transactional
    public TournamentDTO removeTeam(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found: " + tournamentId));
        tournament.getTeams().removeIf(t -> t.getId().equals(teamId));
        tournamentRepository.save(tournament);
        return tournamentMapper.toDTO(tournament);
    }

    @Override
    public List<TeamDTO> getTeams(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found: " + tournamentId));
        return tournament.getTeams().stream().map(teamMapper::toDTO).toList();
    }

    @Override
    @Transactional
    public List<MatchDTO> generateMatches(Long tournamentId, String type, LocalDate startDate, String matchTime) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found: " + tournamentId));

        List<Team> teams = tournament.getTeams();
        if (teams.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 equipos para generar partidos.");
        }

        LocalDate base = startDate != null ? startDate : LocalDate.now();
        LocalTime time = parseTime(matchTime);

        List<Match> created = new ArrayList<>();

        if ("ROUND_ROBIN".equalsIgnoreCase(type)) {
            created = generateRoundRobin(tournament, teams, base, time);
        } else if ("ELIMINATION".equalsIgnoreCase(type)) {
            created = generateElimination(tournament, teams, base, time);
        } else {
            throw new IllegalArgumentException("Tipo inválido: use ROUND_ROBIN o ELIMINATION");
        }

        List<Match> saved = matchRepository.saveAll(created);
        return saved.stream().map(matchMapper::toDTO).toList();
    }

    private List<Match> generateRoundRobin(Tournament tournament, List<Team> teams, LocalDate base, LocalTime time) {
        List<Match> matches = new ArrayList<>();
        int day = 0;
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                matches.add(buildMatch(tournament, teams.get(i), teams.get(j), base.plusDays(day), time));
                day++;
            }
        }
        return matches;
    }

    private List<Match> generateElimination(Tournament tournament, List<Team> teams, LocalDate base, LocalTime time) {
        List<Match> matches = new ArrayList<>();
        int pairs = teams.size() / 2;
        for (int i = 0; i < pairs; i++) {
            matches.add(buildMatch(tournament, teams.get(i * 2), teams.get(i * 2 + 1), base.plusDays(i), time));
        }
        return matches;
    }

    private Match buildMatch(Tournament tournament, Team home, Team away, LocalDate date, LocalTime time) {
        Match match = new Match();
        match.setTournament(tournament);
        match.setSportType(tournament.getSportType());
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setState(MatchState.PENDING);
        match.setPhase(MatchPhase.NOT_STARTED);
        match.setLocation(home.getStadium() != null ? home.getStadium() : "");
        match.setStartFirstTime(LocalDateTime.of(date, time));
        return match;
    }

    private LocalTime parseTime(String matchTime) {
        if (matchTime == null || matchTime.isBlank()) return LocalTime.of(15, 0);
        try {
            String[] parts = matchTime.split(":");
            return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (Exception e) {
            return LocalTime.of(15, 0);
        }
    }
}
