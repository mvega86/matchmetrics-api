package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.PlayerDTO;

import java.util.List;

public interface IPlayerService {
    public PlayerDTO save(PlayerDTO playerDTO);

    List<PlayerDTO> searchPlayers(String search);

    PlayerDTO getById(Long id);

    void delete(Long id);

    PlayerDTO updateStatistic(PlayerDTO playerDTO);
}
