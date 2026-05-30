package com.matchmetrics.mapper;

import com.matchmetrics.mapper.dto.PlayerStatisticDTO;
import com.matchmetrics.persistence.entity.FieldZone;
import com.matchmetrics.persistence.entity.PlayerStatistic;
import com.matchmetrics.persistence.entity.PlayerMatch;
import com.matchmetrics.persistence.entity.Statistic;
import com.matchmetrics.utils.MatchTimeUtil;
import org.springframework.stereotype.Component;

@Component
public class PlayerStatisticMapper {

    private final StatisticMapper statisticMapper;
    private final PlayerMatchMapper playerMatchMapper;
    private final FieldZoneMapper fieldZoneMapper;

    public PlayerStatisticMapper(StatisticMapper statisticMapper, PlayerMatchMapper playerMatchMapper, FieldZoneMapper fieldZoneMapper) {
        this.statisticMapper = statisticMapper;
        this.playerMatchMapper = playerMatchMapper;
        this.fieldZoneMapper = fieldZoneMapper;
    }

    public PlayerStatistic toEntity(PlayerStatisticDTO dto, PlayerMatch playerMatch, Statistic statistic, FieldZone fieldZone) {
        PlayerStatistic playerStatistic = new PlayerStatistic();
        playerStatistic.setId(dto.getId());
        playerStatistic.setPlayerMatch(playerMatch);
        playerStatistic.setStatistic(statistic);
        playerStatistic.setTimestamp(dto.getTimestamp());
        playerStatistic.setPositionX(dto.getPositionX());
        playerStatistic.setPositionY(dto.getPositionY());
        playerStatistic.setObservation(dto.getObservation());
        playerStatistic.setFieldZone(fieldZone);
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
        dto.setFieldZone(playerStatistic.getFieldZone()!=null?fieldZoneMapper.toDTO(playerStatistic.getFieldZone()):null);
        // Calculate relative minute for display only in API
        dto.setRelativeMinuteFormatted(MatchTimeUtil.calculateRelativeMinuteFormatted(playerStatistic.getPlayerMatch().getMatch(), playerStatistic.getTimestamp()));

        return dto;
    }
}


