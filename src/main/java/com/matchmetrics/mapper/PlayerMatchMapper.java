package com.matchmetrics.mapper;

import com.matchmetrics.mapper.dto.PlayerDTO;
import com.matchmetrics.mapper.dto.PlayerMatchDTO;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.entity.Team;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class PlayerMatchMapper {

    private final MatchMapper matchMapper;
    private final PlayerMapper playerMapper;

    public PlayerMatchMapper(@Lazy MatchMapper matchMapper, PlayerMapper playerMapper) {
        this.matchMapper = matchMapper;
        this.playerMapper = playerMapper;
    }
    public PlayerMatch toEntity(PlayerMatchDTO dto, Team team) {
        PlayerMatch playerMatch = new PlayerMatch();
        playerMatch.setId(dto.getId());
        playerMatch.setMatch(matchMapper.toEntity(dto.getMatch()));
        playerMatch.setPlayer(playerMapper.toEntity(dto.getPlayer(), team));
        playerMatch.setInTime(dto.getInTime());
        playerMatch.setOutTime(dto.getOutTime());
        return playerMatch;
    }

    public PlayerMatchDTO toDTO(PlayerMatch playerMatch) {
        PlayerMatchDTO dto = new PlayerMatchDTO();
        dto.setId(playerMatch.getId());

        // 🔹 Se evita anidar `MatchDTO` completo para prevenir recursividad

        if(playerMatch.getMatch() != null) {
            dto.setMatch(matchMapper.toDTO(playerMatch.getMatch()));
        }else{
            dto.setMatch(null);
        }

        // 🔹 Se evita anidar `PlayerDTO` completo para prevenir recursividad
        PlayerDTO playerDTO = playerMapper.toDTO(playerMatch.getPlayer());
        playerDTO.setTeamId(playerMatch.getPlayer().getTeam().getId()); // Evita ciclos recursivos
        dto.setPlayer(playerDTO);

        dto.setInTime(playerMatch.getInTime());
        dto.setOutTime(playerMatch.getOutTime());

        return dto;
    }
}

