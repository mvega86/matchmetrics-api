package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.mapper.dto.TournamentDTO;

import java.util.List;

public interface ITournamentService {
    List<TournamentDTO> search(String search);
    TournamentDTO getById(Long id);
    TournamentDTO create(TournamentDTO dto);
    TournamentDTO update(Long id, TournamentDTO dto);
    void delete(Long id);

    TournamentDTO addTeam(Long tournamentId, Long teamId);
    TournamentDTO removeTeam(Long tournamentId, Long teamId);
    List<TeamDTO> getTeams(Long tournamentId);

    List<MatchDTO> generateMatches(Long tournamentId, String type, java.time.LocalDate startDate, String matchTime);
}
