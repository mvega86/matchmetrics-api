package com.futbol.api_party.service;

import com.futbol.api_party.mapper.dto.PlayerMatchDTO;
import com.futbol.api_party.mapper.dto.PlayerStatisticDTO;

import java.util.List;

public interface IPlayerStatisticService {
    List<PlayerStatisticDTO> search(String search);
    PlayerStatisticDTO createPlayerStatistic(PlayerStatisticDTO playerStatisticDTO);
    List<PlayerStatisticDTO> getStatisticsByPlayerMatch(Long playerMatchId);
    PlayerStatisticDTO update(PlayerStatisticDTO playerStatisticDTO);
    void delete(Long id);
}
