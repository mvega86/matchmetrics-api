package com.matchmetrics.mapper;

import com.matchmetrics.mapper.dto.PlayerDTO;
import com.matchmetrics.persistence.entity.Player;
import com.matchmetrics.persistence.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    private TeamMapper teamMapper;

    public PlayerMapper(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    public PlayerDTO toDTO(Player player){
        return new PlayerDTO(
                player.getId(),
                player.getFullName(),
                player.getJerseyName(),
                player.getJerseyNumber(),
                player.getBirthDate(),
                player.getAge(),
                player.getTeam() != null ? player.getTeam().getId() : null,
                player.getPhotoUrl());
    }
    public Player toEntity(PlayerDTO playerDTO, Team team){
        return new Player(
                playerDTO.getId(),
                playerDTO.getFullName(),
                playerDTO.getJerseyName(),
                playerDTO.getJerseyNumber(),
                playerDTO.getBirthDate(),
                playerDTO.getAge(),
                team,
                playerDTO.getPhotoUrl());
    }
}
