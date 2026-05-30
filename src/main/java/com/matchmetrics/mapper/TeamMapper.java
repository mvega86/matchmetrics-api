package com.matchmetrics.mapper;

import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.persistence.entity.Team;
import org.springframework.stereotype.Component;

// =========================
// MAPPER TeamMapper
// =========================
@Component
public class TeamMapper {
    public TeamDTO toDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setAcronym(team.getAcronym());
        dto.setStadium(team.getStadium());
        return dto;
    }

    public Team toEntity(TeamDTO dto) {
        Team team = new Team();
        team.setId(dto.getId());
        team.setName(dto.getName());
        team.setAcronym(dto.getAcronym());
        team.setStadium(dto.getStadium());
        return team;
    }
}
