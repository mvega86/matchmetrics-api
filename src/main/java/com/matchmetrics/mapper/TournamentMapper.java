package com.matchmetrics.mapper;

import com.matchmetrics.domain.enums.TournamentStatus;
import com.matchmetrics.mapper.dto.TournamentDTO;
import com.matchmetrics.persistence.entity.Tournament;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {

    private final TeamMapper teamMapper;

    public TournamentMapper(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    public Tournament toEntity(TournamentDTO dto) {
        Tournament tournament = new Tournament();
        tournament.setId(dto.getId());
        tournament.setName(dto.getName());
        tournament.setDescription(dto.getDescription());
        tournament.setSportType(dto.getSportType());
        tournament.setStatus(dto.getStatus() != null ? dto.getStatus() : TournamentStatus.UPCOMING);
        tournament.setStartDate(dto.getStartDate());
        tournament.setEndDate(dto.getEndDate());
        tournament.setOrganizer(dto.getOrganizer());
        tournament.setLocation(dto.getLocation());
        tournament.setCountry(dto.getCountry());
        tournament.setCategory(dto.getCategory());
        tournament.setLogoUrl(dto.getLogoUrl());
        return tournament;
    }

    public TournamentDTO toDTO(Tournament entity) {
        if (entity == null) return null;
        TournamentDTO dto = new TournamentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setSportType(entity.getSportType());
        dto.setStatus(entity.getStatus());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setOrganizer(entity.getOrganizer());
        dto.setLocation(entity.getLocation());
        dto.setCountry(entity.getCountry());
        dto.setCategory(entity.getCategory());
        dto.setMatchCount(entity.getMatches() != null ? entity.getMatches().size() : 0);
        if (entity.getTeams() != null) {
            dto.setTeams(entity.getTeams().stream().map(teamMapper::toDTO).toList());
        }
        return dto;
    }
}
