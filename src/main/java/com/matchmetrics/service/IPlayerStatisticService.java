package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.PlayerStatisticDTO;

import java.util.List;

public interface IPlayerStatisticService {
    List<PlayerStatisticDTO> search(String search);
    List<PlayerStatisticDTO> searchByTeam(String search, Long teamId);
    PlayerStatisticDTO createPlayerStatistic(PlayerStatisticDTO playerStatisticDTO);
    List<PlayerStatisticDTO> getStatisticsByPlayerMatch(Long playerMatchId);
    PlayerStatisticDTO update(PlayerStatisticDTO playerStatisticDTO);
    void delete(Long id);
}
