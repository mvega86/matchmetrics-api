package com.matchmetrics.mapper;

import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.persistence.entity.Match;
import com.matchmetrics.persistence.entity.Tournament;
import com.matchmetrics.persistence.repository.TournamentRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

    private final TeamMapper teamMapper;
    private final PlayerMatchMapper playerMatchMapper;
    private final TournamentRepository tournamentRepository;

    public MatchMapper(TeamMapper teamMapper, @Lazy PlayerMatchMapper playerMatchMapper, TournamentRepository tournamentRepository) {
        this.teamMapper = teamMapper;
        this.playerMatchMapper = playerMatchMapper;
        this.tournamentRepository = tournamentRepository;
    }

    public Match toEntity(MatchDTO dto) {
        Match match = new Match();
        match.setId(dto.getId());
        match.setSportType(dto.getSportType() != null ? dto.getSportType() : SportType.FOOTBALL);
        match.setLocation(dto.getLocation());
        match.setState(dto.getState() != null ? dto.getState() : MatchState.PENDING);
        match.setPhase(dto.getPhase());
        match.setHomeTeam(teamMapper.toEntity(dto.getHomeTeam()));
        match.setAwayTeam(teamMapper.toEntity(dto.getAwayTeam()));
        match.setStartFirstTime(dto.getStartFirstTime());
        match.setEndFirstTime(dto.getEndFirstTime());
        match.setStartSecondTime(dto.getStartSecondTime());
        match.setEndSecondTime(dto.getEndSecondTime());
        match.setStartFirstExtraTime(dto.getStartFirstExtraTime());
        match.setEndFirstExtraTime(dto.getEndFirstExtraTime());
        match.setStartSecondExtraTime(dto.getStartSecondExtraTime());
        match.setEndSecondExtraTime(dto.getEndSecondExtraTime());
        if (dto.getTournamentId() != null) {
            Tournament tournament = tournamentRepository.findById(dto.getTournamentId()).orElse(null);
            match.setTournament(tournament);
        }
        return match;
    }

    public MatchDTO toDTO(Match match) {
        if (match == null) return null;

        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setId(match.getId());
        matchDTO.setSportType(match.getSportType());
        matchDTO.setLocation(match.getLocation());
        matchDTO.setState(match.getState());
        matchDTO.setPhase(match.getPhase());
        matchDTO.setHomeTeam(teamMapper.toDTO(match.getHomeTeam()));
        matchDTO.setAwayTeam(teamMapper.toDTO(match.getAwayTeam()));
        matchDTO.setStartFirstTime(match.getStartFirstTime());
        matchDTO.setEndFirstTime(match.getEndFirstTime());
        matchDTO.setStartSecondTime(match.getStartSecondTime());
        matchDTO.setEndSecondTime(match.getEndSecondTime());
        matchDTO.setStartFirstExtraTime(match.getStartFirstExtraTime());
        matchDTO.setEndFirstExtraTime(match.getEndFirstExtraTime());
        matchDTO.setStartSecondExtraTime(match.getStartSecondExtraTime());
        matchDTO.setEndSecondExtraTime(match.getEndSecondExtraTime());
        if (match.getTournament() != null) {
            matchDTO.setTournamentId(match.getTournament().getId());
            matchDTO.setTournamentName(match.getTournament().getName());
        }
        return matchDTO;
    }
}
