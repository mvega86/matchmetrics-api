package com.matchmetrics.mapper;

import com.matchmetrics.mapper.dto.PlayerDTO;
import com.matchmetrics.persistence.entity.Player;
import com.matchmetrics.persistence.entity.Team;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlayerMapper {

    private TeamMapper teamMapper;

    public PlayerMapper(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    public PlayerDTO toDTO(Player player) {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(player.getId());
        dto.setFullName(player.getFullName());
        dto.setJerseyName(player.getJerseyName());
        dto.setJerseyNumber(player.getJerseyNumber());
        dto.setBirthDate(player.getBirthDate());
        dto.setAge(player.getAge());
        dto.setTeamId(player.getTeam() != null ? player.getTeam().getId() : null);
        dto.setTeamIds(
            player.getTeams() != null
                ? player.getTeams().stream().map(Team::getId).collect(Collectors.toList())
                : List.of()
        );
        dto.setPhotoUrl(player.getPhotoUrl());
        dto.setFieldPosition(player.getFieldPosition());
        return dto;
    }

    public Player toEntity(PlayerDTO playerDTO, Team team) {
        Player p = new Player();
        p.setId(playerDTO.getId());
        p.setFullName(playerDTO.getFullName());
        p.setJerseyName(playerDTO.getJerseyName());
        p.setJerseyNumber(playerDTO.getJerseyNumber());
        p.setBirthDate(playerDTO.getBirthDate());
        p.setTeam(team);
        p.setPhotoUrl(playerDTO.getPhotoUrl());
        p.setFieldPosition(playerDTO.getFieldPosition());
        return p;
    }
}
