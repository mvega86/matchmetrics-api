package com.futbol.api_party.mapper;

import com.futbol.api_party.mapper.dto.PlayerStatisticDTO;
import com.futbol.api_party.persistence.entity.PlayerStatistic;
import com.futbol.api_party.persistence.entity.PlayerMatch;
import com.futbol.api_party.persistence.entity.Statistic;
import com.futbol.api_party.utils.MatchTimeUtil;
import org.springframework.stereotype.Component;

@Component
public class PlayerStatisticMapper {

    private final StatisticMapper statisticMapper;
    private final PlayerMatchMapper playerMatchMapper;

    public PlayerStatisticMapper(StatisticMapper statisticMapper, PlayerMatchMapper playerMatchMapper) {
        this.statisticMapper = statisticMapper;
        this.playerMatchMapper = playerMatchMapper;
    }

    public PlayerStatistic toEntity(PlayerStatisticDTO dto, PlayerMatch playerMatch, Statistic statistic) {
        PlayerStatistic playerStatistic = new PlayerStatistic();
        playerStatistic.setId(dto.getId());
        playerStatistic.setPlayerMatch(playerMatch);
        playerStatistic.setStatistic(statistic);
        playerStatistic.setTimestamp(dto.getTimestamp());
        playerStatistic.setPositionX(dto.getPositionX());
        playerStatistic.setPositionY(dto.getPositionY());
        playerStatistic.setObservation(dto.getObservation());
        return playerStatistic;
    }

    public PlayerStatisticDTO toDTO(PlayerStatistic playerStatistic) {
        PlayerStatisticDTO dto = new PlayerStatisticDTO();
        dto.setId(playerStatistic.getId());
        dto.setPlayerMatch(playerMatchMapper.toDTO(playerStatistic.getPlayerMatch()));
        dto.setStatistic(statisticMapper.toDTO(playerStatistic.getStatistic()));
        dto.setTimestamp(playerStatistic.getTimestamp());
        dto.setPositionX(playerStatistic.getPositionX());
        dto.setPositionY(playerStatistic.getPositionY());
        dto.setObservation(playerStatistic.getObservation());

        // Calculate relative minute for display only in API
        dto.setRelativeMinuteFormatted(MatchTimeUtil.calculateRelativeMinuteFormatted(playerStatistic.getPlayerMatch().getMatch(), playerStatistic.getTimestamp()));

        return dto;
    }
}


