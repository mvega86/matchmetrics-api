package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.PlayerDTO;

import java.util.List;

public interface IPlayerService {

    PlayerDTO save(PlayerDTO playerDTO);

    List<PlayerDTO> searchPlayers(String search);

    List<PlayerDTO> searchPlayersByTeam(String search, Long teamId);

    PlayerDTO getById(Long id);

    void delete(Long id);

    PlayerDTO updateStatistic(PlayerDTO playerDTO);
}