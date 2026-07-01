package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.PlayerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPlayerService {

    PlayerDTO save(PlayerDTO playerDTO);

    List<PlayerDTO> searchPlayers(String search);

    List<PlayerDTO> searchPlayersByTeam(String search, Long teamId);

    Page<PlayerDTO> searchPlayersPage(String search, Pageable pageable);

    Page<PlayerDTO> searchPlayersByTeamPage(String search, Long teamId, Pageable pageable);

    PlayerDTO getById(Long id);

    void delete(Long id);

    PlayerDTO updateStatistic(PlayerDTO playerDTO);
}